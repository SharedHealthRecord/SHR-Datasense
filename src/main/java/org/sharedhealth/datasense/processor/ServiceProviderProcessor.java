package org.sharedhealth.datasense.processor;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.datasense.client.FacilityWebClient;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.model.fhir.ServiceProviderReference;
import org.sharedhealth.datasense.repository.FacilityDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("serviceProviderProcessor")
public class ServiceProviderProcessor implements ResourceProcessor {

    private ResourceProcessor nextProcessor;
    private FacilityDao facilityDao;
    private FacilityWebClient facilityWebClient;
    private PropertiesFactoryBean dhisFacilitiesMap;
    private ProviderProcessor providerProcessor;
    private DatasenseProperties datasenseProperties;
    private static final Logger logger = LoggerFactory.getLogger(ServiceProviderProcessor.class);

    @Autowired
    public ServiceProviderProcessor(@Qualifier("clinicalEncounterProcessor") ResourceProcessor nextProcessor,
                                    FacilityDao facilityDao,
                                    FacilityWebClient facilityWebClient,
                                    @Qualifier("dhisFacilitiesMap") PropertiesFactoryBean dhisFacilitiesMap,
                                    ProviderProcessor providerProcessor, DatasenseProperties datasenseProperties) {

        this.nextProcessor = nextProcessor;
        this.facilityDao = facilityDao;
        this.facilityWebClient = facilityWebClient;
        this.dhisFacilitiesMap = dhisFacilitiesMap;
        this.providerProcessor = providerProcessor;
        this.datasenseProperties = datasenseProperties;
    }

    @Override
    public void process(EncounterComposition composition) {
        logger.info("Resolving service provider for encounter of patient:" + composition.getPatientReference().getHealthId());
        String facilityId = null;
        Facility facility = null;
        ServiceProviderReference serviceProviderReference = composition.getServiceProviderReference();
        if (serviceProviderReference != null && serviceProviderReference.getReference() != null) {
            facilityId = serviceProviderReference.getFacilityId();
        }
        facility = loadFacilityFromServiceProvider(facilityId);

        if (facility == null) {
            facility = loadFacilityFromParticipant(composition);
        }
        setFacilityValue(composition, facility);
        callNextIfGiven(composition);
    }

    private boolean isBahmniCloud(String facilityId) {
        return datasenseProperties.getCloudHostedFacilityIds().contains(facilityId);
    }

    private Facility loadFacilityFromParticipant(EncounterComposition composition) {
        String facilityId = providerProcessor.process(composition);
        if (facilityId != null) {
            return findFacility(facilityId);
        }
        return null;
    }

    private Facility loadFacilityFromServiceProvider(String facilityId) {
        if (StringUtils.isNotBlank(facilityId)) {
            return findFacility(facilityId);
        }
        return null;
    }

    private void setFacilityValue(EncounterComposition composition, Facility facility) {
        if (facility != null) {
            composition.getServiceProviderReference().setValue(facility);
        }
    }

    private Facility findFacility(String facilityId) {
        Facility facility = facilityDao.findFacilityById(facilityId);
        if (facility == null) {
            facility = downloadAndSaveFacility(facilityId);
        }
        return facility;
    }

    private void callNextIfGiven(EncounterComposition bundle) {
        if (nextProcessor != null) {
            logger.info("Invoking next processor:" + nextProcessor.getClass().getName());
            nextProcessor.process(bundle);
        }
    }

    private Facility downloadAndSaveFacility(final String facilityId) {
        Facility facility = null;
        try {
            facility = facilityWebClient.findById(facilityId);
        } catch (Exception e) {
            logger.error("Can not find facility with Id:" + facilityId, e);
        }

        if (facility == null) {
            return null;
        }
        facility.setDhisOrgUnitUid(identifyDhisOrgUnitUid(facilityId));
        facilityDao.save(facility);
        return facility;
    }

    private String identifyDhisOrgUnitUid(String facilityId) {
        try {
            return (String) dhisFacilitiesMap.getObject().get(facilityId);
        } catch (IOException e) {
            logger.error(String.format("DHIS Organisation Unit Uid not found for facility %s", facilityId), e);
        }
        return null;
    }

    @Override
    public void setNext(ResourceProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }
}
