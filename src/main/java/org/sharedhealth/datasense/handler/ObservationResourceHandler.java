package org.sharedhealth.datasense.handler;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import org.sharedhealth.datasense.config.DatasenseProperties;
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
    public boolean canHandle(IResource resource) {
        if (!(resource instanceof ca.uhn.fhir.model.dstu2.resource.Observation)) {
            return false;
        } else {
            List<CodingDt> codingDts = ((ca.uhn.fhir.model.dstu2.resource.Observation) resource).getCode().getCoding();
            for (CodingDt coding : codingDts) {
                if (configurationService.getDeathCodes().contains(coding.getCode())) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public void process(IResource resource, EncounterComposition composition) {
        Observation observation = new Observation();
        mapObservation(composition, observation, resource, null);
    }

    @Override
    public void deleteExisting(EncounterComposition composition) {
        String encounterId = composition.getEncounterReference().getValue().getEncounterId();
        String healthId = composition.getPatientReference().getHealthId();
        observationDao.deleteExisting(healthId, encounterId);

    }

    private void mapRelatedComponents(EncounterComposition composition, String parentObsUUID,
                                      ca.uhn.fhir.model.dstu2.resource.Observation fhirObservation, Integer reportId) {

        List<ca.uhn.fhir.model.dstu2.resource.Observation.Related> relatedObservations = fhirObservation.getRelated();
        for (ca.uhn.fhir.model.dstu2.resource.Observation.Related relatedObservation : relatedObservations) {
            IResource resource = composition.getContext().getResourceForReference(relatedObservation.getTarget());
            Observation childObservation;
            childObservation = new Observation();
            mapObservation(composition, childObservation, resource, reportId);
            childObservation.setParentId(parentObsUUID);
            observationDao.updateParentId(childObservation);
        }
    }

    public void mapObservation(EncounterComposition composition, Observation observation, IResource
            resource, Integer report_id) {
        ca.uhn.fhir.model.dstu2.resource.Observation fhirObservation =
                (ca.uhn.fhir.model.dstu2.resource.Observation) resource;

        List<CodingDt> codings = fhirObservation.getCode().getCoding();
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
            if(null != report_id) {
                observation.setReportId(report_id);
            }
            observation.setObservationId(observationDao.save(observation));
            parentObsUUID = observation.getUuid();
        }
        mapRelatedComponents(composition, parentObsUUID, fhirObservation, report_id);
    }
}
