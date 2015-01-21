package org.sharedhealth.datasense.handler;

import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Observation;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.ObservationDao;
import org.sharedhealth.datasense.handler.mappers.ObservationValueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.sharedhealth.datasense.util.FhirCodeLookupService.getConceptId;
import static org.sharedhealth.datasense.util.FhirCodeLookupService.getReferenceCode;

@Component
public class ObservationResourceHandler implements FhirResourceHandler {
    private ObservationDao observationDao;
    private ObservationValueMapper observationValueMapper;

    @Autowired
    public ObservationResourceHandler(ObservationDao observationDao) {
        this.observationDao = observationDao;
        this.observationValueMapper = new ObservationValueMapper();
    }

    @Override
    public boolean canHandle(Resource resource) {
        return resource.getResourceType().equals(ResourceType.Observation);
    }

    @Override
    public void process(Resource resource, EncounterComposition composition) {
        org.hl7.fhir.instance.model.Observation fhirObservation = (org.hl7.fhir.instance.model.Observation) resource;
        Observation observation = new Observation();
        Encounter encounter = composition.getEncounterReference().getValue();
        observation.setEncounter(encounter);
        observation.setPatient(composition.getPatientReference().getValue());
        observation.setDatetime(composition.getEncounterReference().getValue().getEncounterDateTime());

        List<Coding> codings = fhirObservation.getName().getCoding();
        observation.setConceptId(getConceptId(codings));
        observation.setReferenceCode(getReferenceCode(codings));

        observation.setValue(observationValueMapper.getObservationValue(fhirObservation.getValue()));
        observationDao.save(observation);
    }
}
