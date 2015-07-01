package org.sharedhealth.datasense.handler;

import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Resource;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.handler.mappers.ObservationValueMapper;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Observation;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.ObservationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.hl7.fhir.instance.model.Observation.ObservationRelatedComponent;
import static org.sharedhealth.datasense.util.FhirCodeLookup.getConceptId;
import static org.sharedhealth.datasense.util.FhirCodeLookup.getReferenceCode;

@Component
public class ObservationResourceHandler implements FhirResourceHandler {
    private ObservationDao observationDao;
    private ObservationValueMapper observationValueMapper;
    private DatasenseProperties datasenseProperties;

    @Autowired
    public ObservationResourceHandler(ObservationDao observationDao, DatasenseProperties datasenseProperties) {
        this.observationDao = observationDao;
        this.datasenseProperties = datasenseProperties;
        this.observationValueMapper = new ObservationValueMapper();
    }

    @Override
    public boolean canHandle(Resource resource) {
        if (!(resource instanceof org.hl7.fhir.instance.model.Observation)) {
            return false;
        } else {
            for (Coding coding : ((org.hl7.fhir.instance.model.Observation) resource).getName().getCoding()) {
                if (datasenseProperties.getDeathCodes().contains(coding.getCodeSimple())) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public void process(Resource resource, EncounterComposition composition) {
        Observation observation = new Observation();
        mapObservation(composition, observation, resource);
    }

    @Override
    public void deleteExisting(EncounterComposition composition) {
        String encounterId = composition.getEncounterReference().getValue().getEncounterId();
        String healthId = composition.getPatientReference().getHealthId();
        observationDao.deleteExisting(healthId, encounterId);

    }

    private void mapRelatedComponents(EncounterComposition composition, String parentObsUUID, org.hl7.fhir
            .instance.model.Observation fhirObservation) {
        for (ObservationRelatedComponent relatedComponent : fhirObservation.getRelated()) {
            Resource resource = composition.getContext().getResourceByReferenceFromFeed(relatedComponent.getTarget());
            Observation childObservation;
            childObservation = new Observation();
            mapObservation(composition, childObservation, resource);
            childObservation.setParentId(parentObsUUID);
            observationDao.updateParentId(childObservation);
        }
    }

    private void mapObservation(EncounterComposition composition, Observation observation, Resource
            resource) {
        org.hl7.fhir.instance.model.Observation fhirObservation = (org.hl7.fhir.instance.model.Observation)
                resource;

        List<Coding> codings = fhirObservation.getName().getCoding();
        String conceptId = getConceptId(codings);
        String parentObsUUID = null;
        if (conceptId != null) {
            observation.setConceptId(conceptId);
            observation.setReferenceCode(getReferenceCode(codings));

            Encounter encounter = composition.getEncounterReference().getValue();
            observation.setEncounter(encounter);
            observation.setPatient(composition.getPatientReference().getValue());
            observation.setDatetime(composition.getEncounterReference().getValue().getEncounterDateTime());

            observation.setValue(observationValueMapper.getObservationValue(fhirObservation.getValue()));

            observation.setObservationId(observationDao.save(observation));
            parentObsUUID = observation.getUuid();
        }
        mapRelatedComponents(composition, parentObsUUID, fhirObservation);
    }
}
