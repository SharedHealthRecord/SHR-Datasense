package org.sharedhealth.datasense.processors;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.datasense.client.FacilityWebClient;
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.FacilityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;

@Component("serviceProviderProcessor")
public class ServiceProviderProcessor implements ResourceProcessor {

    private ResourceProcessor nextProcessor;
    private FacilityDao facilityDao;
    private FacilityWebClient webClient;

    @Autowired
    public ServiceProviderProcessor(@Qualifier("clinicalEncounterProcessor") ResourceProcessor nextProcessor,
                                    FacilityDao facilityDao, FacilityWebClient webClient) {
        this.nextProcessor = nextProcessor;
        this.facilityDao = facilityDao;
        this.webClient = webClient;
    }

    @Override
    public void process(EncounterComposition composition) {
        String facilityId = composition.getServiceProviderReference().getFacilityId();
        if (StringUtils.isNotBlank(facilityId)) {
            Facility facility = facilityDao.findFacilityById(facilityId);
            if (facility == null) {
                facility = downloadAndSaveFacility(facilityId);
            }
            composition.getServiceProviderReference().setValue(facility);
        }
        callNextIfGiven(composition);
    }

    private void callNextIfGiven(EncounterComposition bundle) {
        if (nextProcessor != null) {
            nextProcessor.process(bundle);
        }
    }

    private Facility downloadAndSaveFacility(String facilityId) {
        Facility facility;
        try {
            facility = webClient.findById(facilityId);
        } catch (IOException e) {
            throw new RuntimeException("Can not find facility with Id:" + facilityId);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Can not find facility with Id:" + facilityId);
        }

        if (facility == null) {
            //TODO remove after bahmni sends proper facility id
            return null;
            //throw new RuntimeException("Can not find facility with Id:" + facilityId);

        }
        facilityDao.save(facility);
        return facility;
    }

    @Override
    public void setNext(ResourceProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }
}
