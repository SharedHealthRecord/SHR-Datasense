package org.sharedhealth.datasense.processor;

import ca.uhn.fhir.model.dstu2.composite.BoundCodeableConceptDt;
import ca.uhn.fhir.model.dstu2.valueset.EncounterTypeEnum;
import org.apache.log4j.Logger;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.model.Patient;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.model.fhir.ServiceProviderReference;
import org.sharedhealth.datasense.repository.EncounterDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component("clinicalEncounterProcessor")
public class ClinicalEncounterProcessor implements ResourceProcessor {
    public static final String UNKNOWN_ENCOUNTER_TYPE = "unknown";
    private EncounterDao encounterDao;
    private ResourceProcessor nextProcessor;

    private Logger log = Logger.getLogger(ClinicalEncounterProcessor.class);

    @Autowired
    public ClinicalEncounterProcessor(@Qualifier("subResourceProcessor") ResourceProcessor nextProcessor,
                                      EncounterDao encounterDao) {
        this.nextProcessor = nextProcessor;
        this.encounterDao = encounterDao;
    }

    @Override
    public void process(EncounterComposition composition) {
        log.info("Processing encounters for patient:" + composition.getPatientReference().getHealthId());
        ca.uhn.fhir.model.dstu2.resource.Encounter fhirEncounter = composition.getEncounterReference().getResource();
        Encounter encounter = mapEncounterFields(fhirEncounter, composition);
        composition.getEncounterReference().setValue(encounter);
        encounterDao.deleteExisting(composition.getPatientReference().getHealthId(), composition.getEncounterReference().getEncounterId());
        encounterDao.save(encounter);
        if (nextProcessor != null) {
            log.info("Invoking next processor:" + nextProcessor.getClass().getName());
            nextProcessor.process(composition);
        }
    }

    private Encounter mapEncounterFields(ca.uhn.fhir.model.dstu2.resource.Encounter fhirEncounter, EncounterComposition
            composition) {
        Encounter encounter = new Encounter();
        encounter.setEncounterType(getEncounterType(fhirEncounter));
        encounter.setEncounterId(composition.getContext().getShrEncounterId());
        encounter.setEncounterVisitType(getVisitType(fhirEncounter));
        Patient patient = composition.getPatientReference().getValue();
        encounter.setPatient(patient);
        ServiceProviderReference facilityReference = composition.getServiceProviderReference();
        if (facilityReference != null && facilityReference.getValue() != null) {
            Facility facility = facilityReference.getValue();
            encounter.setFacility(facility);
            encounter.setLocationCode(facility.getFacilityLocationCode());
        }
        Date encounterDate = composition.getComposition().getDate();
        encounter.setEncounterDateTime(encounterDate);
        return encounter;
    }

    private String getVisitType(ca.uhn.fhir.model.dstu2.resource.Encounter fhirEncounter) {
        return fhirEncounter.getClassElement();
    }

    private String getEncounterType(ca.uhn.fhir.model.dstu2.resource.Encounter fhirEncounter) {
        List<BoundCodeableConceptDt<EncounterTypeEnum>> types = fhirEncounter.getType();
        if (types.isEmpty()) {
            return UNKNOWN_ENCOUNTER_TYPE;
        }
        //TODO - check for codeable concept what values we get, especially when not enum, as this is example
        return types.get(0).getText();
    }

    @Override
    public void setNext(ResourceProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

}