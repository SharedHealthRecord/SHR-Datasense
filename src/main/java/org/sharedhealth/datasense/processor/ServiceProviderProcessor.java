package org.sharedhealth.datasense.processor;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.datasense.client.FacilityWebClient;
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.FacilityDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;

@Component("serviceProviderProcessor")
public class ServiceProviderProcessor implements ResourceProcessor {

    private ResourceProcessor nextProcessor;
    private FacilityDao facilityDao;
    private FacilityWebClient facilityWebClient;
    private PropertiesFactoryBean dhisFacilitiesMap;
    private static final Logger logger = LoggerFactory.getLogger(ServiceProviderProcessor.class);

    @Autowired
    public ServiceProviderProcessor(@Qualifier("clinicalEncounterProcessor") ResourceProcessor nextProcessor,
                                    FacilityDao facilityDao, FacilityWebClient facilityWebClient,
                                    @Qualifier("dhisFacilitiesMap") PropertiesFactoryBean dhisFacilitiesMap) {

        this.nextProcessor = nextProcessor;
        this.facilityDao = facilityDao;
        this.facilityWebClient = facilityWebClient;
        this.dhisFacilitiesMap = dhisFacilitiesMap;
    }

    @Override
    public void process(EncounterComposition composition) {
        String facilityId = parseUrl(composition.getServiceProviderReference().getFacilityId());
        if (StringUtils.isNotBlank(facilityId)) {
            Facility facility = facilityDao.findFacilityById(facilityId);
            if (facility == null) {
                facility = downloadAndSaveFacility(facilityId);
            }
            if(facility != null) {
                composition.getServiceProviderReference().setValue(facility);
            }
        }
        callNextIfGiven(composition);
    }

    private void callNextIfGiven(EncounterComposition bundle) {
        if (nextProcessor != null) {
            nextProcessor.process(bundle);
        }
    }

    private Facility downloadAndSaveFacility(final String facilityId) {
        Facility facility;
        try {
            facility = facilityWebClient.findById(facilityId);
        } catch (Exception e) {
            logger.error("Can not find facility with Id:" + facilityId, e);
            throw new RuntimeException("Can not find facility with Id:" + facilityId, e);
        }

        if (facility == null) {
            //TODO remove after bahmni sends proper facility id
            return null;
            //throw new RuntimeException("Can not find facility with Id:" + facilityId);
        }
        facility.setDhisOrgUnitUid(identifyDhisOrgUnitUid(facilityId));
        facilityDao.save(facility);
        return facility;
    }

    private String identifyDhisOrgUnitUid(String facilityId) {
        try {
            return (String) dhisFacilitiesMap.getObject().get(facilityId);
        } catch (IOException e) {
            logger.error(String.format("DHIS Organisation Unit Uid not found for facility %s", facilityId),e);
        }
        return null;
    }

    @Override
    public void setNext(ResourceProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    protected String parseUrl(String facilityUrl) {
        String s = StringUtils.substringAfterLast(facilityUrl, "/");
        return StringUtils.substringBefore(s, ".json");
    }
}
