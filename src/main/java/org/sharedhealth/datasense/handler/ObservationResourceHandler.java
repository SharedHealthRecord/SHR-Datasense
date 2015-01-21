package org.sharedhealth.datasense.handler;

import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.sharedhealth.datasense.handler.mappers.ObservationValueMapper;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Observation;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.ObservationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.hl7.fhir.instance.model.Observation.ObservationRelatedComponent;
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
        Observation observation = new Observation();
        org.hl7.fhir.instance.model.Observation fhirObservation = (org.hl7.fhir.instance.model.Observation) resource;
        mapObservation(composition, observation, fhirObservation);
        List<Observation> childObservations = mapRelatedComponents(composition, observation, fhirObservation);
        saveObservations(observation, childObservations);
    }

    private void saveObservations(Observation observation, List<Observation> childObservations) {
        observationDao.save(observation);
        for (Observation childObservation : childObservations) {
            observationDao.save(childObservation);
        }
    }

    private List<Observation> mapRelatedComponents(EncounterComposition composition, Observation observation, org.hl7.fhir.instance.model.Observation fhirObservation) {
        List<Observation> childObservations= new ArrayList<>();
        for (ObservationRelatedComponent relatedComponent : fhirObservation.getRelated()) {
            Resource relatedFhirObservation = composition.getContext().getResourceByReference(relatedComponent.getTarget());
            if (relatedFhirObservation != null) {
                Observation childObservation = new Observation();
                mapObservation(composition, childObservation, (org.hl7.fhir.instance.model.Observation) relatedFhirObservation);
                childObservation.setParentId(observation.getUuid());
                childObservations.add(childObservation);
            }
        }
        return childObservations;
    }

    private void mapObservation(EncounterComposition composition, Observation observation, org.hl7.fhir.instance.model.Observation fhirObservation) {
        Encounter encounter = composition.getEncounterReference().getValue();
        observation.setEncounter(encounter);
        observation.setPatient(composition.getPatientReference().getValue());
        observation.setDatetime(composition.getEncounterReference().getValue().getEncounterDateTime());

        List<Coding> codings = fhirObservation.getName().getCoding();
        observation.setConceptId(getConceptId(codings));
        observation.setReferenceCode(getReferenceCode(codings));

        observation.setValue(observationValueMapper.getObservationValue(fhirObservation.getValue()));
    }
}
