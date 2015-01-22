package org.sharedhealth.datasense.handler;

import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.sharedhealth.datasense.handler.mappers.ObservationValueMapper;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Observation;
import org.sharedhealth.datasense.model.fhir.DatasenseResourceReference;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.ObservationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.hl7.fhir.instance.model.Observation.ObservationRelatedComponent;
import static org.sharedhealth.datasense.util.FhirCodeLookupService.getConceptId;
import static org.sharedhealth.datasense.util.FhirCodeLookupService.getReferenceCode;
import static org.sharedhealth.datasense.util.ResourceLookupService.getDatasenseResourceReference;

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
    public void process(DatasenseResourceReference datasenseResourceReference, EncounterComposition composition) {
        Observation observation = new Observation();
        mapObservation(composition, observation, datasenseResourceReference);
    }

    private void mapRelatedComponents(EncounterComposition composition, Observation observation, org.hl7.fhir.instance.model.Observation fhirObservation) {
        for (ObservationRelatedComponent relatedComponent : fhirObservation.getRelated()) {
            DatasenseResourceReference datasenseResourceReference = getDatasenseResourceReference(relatedComponent.getTarget(), composition);
            Observation childObservation = null;
            if (datasenseResourceReference.getValue() == null) {
                childObservation = new Observation();
                mapObservation(composition, childObservation, datasenseResourceReference);
            } else {
                childObservation = (Observation) datasenseResourceReference.getValue();
            }
            childObservation.setParentId(observation.getUuid());
            observationDao.updateParentId(childObservation);
        }
    }

    private void mapObservation(EncounterComposition composition, Observation observation, DatasenseResourceReference datasenseResourceReference) {
        org.hl7.fhir.instance.model.Observation fhirObservation = (org.hl7.fhir.instance.model.Observation) datasenseResourceReference.getResourceValue();

        List<Coding> codings = fhirObservation.getName().getCoding();
        String conceptId = getConceptId(codings);
        if(conceptId == null) return;
        observation.setConceptId(conceptId);
        observation.setReferenceCode(getReferenceCode(codings));

        Encounter encounter = composition.getEncounterReference().getValue();
        observation.setEncounter(encounter);
        observation.setPatient(composition.getPatientReference().getValue());
        observation.setDatetime(composition.getEncounterReference().getValue().getEncounterDateTime());

        observation.setValue(observationValueMapper.getObservationValue(fhirObservation.getValue()));

        observation.setObservationId(observationDao.save(observation));
        datasenseResourceReference.setValue(observation);
        mapRelatedComponents(composition, observation, fhirObservation);
    }
}
