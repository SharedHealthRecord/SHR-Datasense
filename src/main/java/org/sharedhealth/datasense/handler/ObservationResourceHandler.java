package org.sharedhealth.datasense.handler;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Resource;
import org.sharedhealth.datasense.handler.mappers.ObservationValueMapper;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Observation;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.ObservationDao;
import org.sharedhealth.datasense.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.sharedhealth.datasense.util.FhirCodeLookup.getConceptId;
import static org.sharedhealth.datasense.util.FhirCodeLookup.getReferenceCode;

@Component
public class ObservationResourceHandler implements FhirResourceHandler {
    private ObservationDao observationDao;
    private ObservationValueMapper observationValueMapper;
    private ConfigurationService configurationService;

    @Autowired
    public ObservationResourceHandler(ObservationDao observationDao, ConfigurationService configurationService) {
        this.observationDao = observationDao;
        this.configurationService = configurationService;
        this.observationValueMapper = new ObservationValueMapper();
    }

    @Override
    public boolean canHandle(Resource resource) {
        if (!(resource instanceof org.hl7.fhir.dstu3.model.Observation)) {
            return false;
        } else {
            List<Coding> codingDts = ((org.hl7.fhir.dstu3.model.Observation) resource).getCode().getCoding();
            for (Coding coding : codingDts) {
                if (configurationService.getDeathCodes().contains(coding.getCode())) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public void process(Resource resource, EncounterComposition composition) {
        Observation observation = new Observation();
        mapObservation(composition, observation, resource, null);
    }

    @Override
    public void deleteExisting(EncounterComposition composition) {
        String encounterId = composition.getEncounterReference().getValue().getEncounterId();
        observationDao.deleteExisting(encounterId);

    }

    private void mapRelatedComponents(EncounterComposition composition, String parentObsUUID,
                                      org.hl7.fhir.dstu3.model.Observation fhirObservation, Integer reportId) {

        List<org.hl7.fhir.dstu3.model.Observation.ObservationRelatedComponent> relatedObservations = fhirObservation.getRelated();
        for (org.hl7.fhir.dstu3.model.Observation.ObservationRelatedComponent relatedObservation : relatedObservations) {
            Resource resource = composition.getContext().getResourceForReference(relatedObservation.getTarget());
            Observation childObservation;
            childObservation = new Observation();
            mapObservation(composition, childObservation, resource, reportId);
            childObservation.setParentId(parentObsUUID);
            observationDao.updateParentId(childObservation);
        }
    }

    public void mapObservation(EncounterComposition composition, Observation observation, Resource
            resource, Integer reportId) {
        org.hl7.fhir.dstu3.model.Observation fhirObservation =
                (org.hl7.fhir.dstu3.model.Observation) resource;

        List<Coding> codings = fhirObservation.getCode().getCoding();
        String conceptId = getConceptId(codings);
        String code = getReferenceCode(codings);
        String parentObsUUID = null;
        if ( (conceptId != null) || (code != null)) {
            observation.setConceptId(conceptId);
            observation.setReferenceCode(code);

            Encounter encounter = composition.getEncounterReference().getValue();
            observation.setEncounter(encounter);
            observation.setPatient(composition.getPatientReference().getValue());
            observation.setDatetime(composition.getEncounterReference().getValue().getEncounterDateTime());

            observation.setValue(observationValueMapper.getObservationValue(fhirObservation.getValue()));
            if(null != reportId) {
                observation.setReportId(reportId);
            }
            observation.setObservationId(observationDao.save(observation));
            parentObsUUID = observation.getUuid();
        }
        mapRelatedComponents(composition, parentObsUUID, fhirObservation, reportId);
    }
}
