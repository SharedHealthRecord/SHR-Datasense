package org.sharedhealth.datasense.handler;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.handler.mappers.ObservationValueMapper;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.PatientDeathDetails;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.PatientDeathDetailsDao;
import org.sharedhealth.datasense.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static org.sharedhealth.datasense.util.TrUrl.isConceptUrl;
import static org.sharedhealth.datasense.util.TrUrl.isReferenceTermUrl;

@Component
public class DeathNoteHandler implements FhirResourceHandler {

    private DatasenseProperties datasenseProperties;
    private PatientDeathDetailsDao patientDeathDetailsDao;
    private ObservationValueMapper observationValueMapper;


    @Autowired
    public DeathNoteHandler(DatasenseProperties datasenseProperties, PatientDeathDetailsDao patientDeathDetailsDao) {
        this.datasenseProperties = datasenseProperties;
        this.patientDeathDetailsDao = patientDeathDetailsDao;
        this.observationValueMapper = new ObservationValueMapper();
    }

    @Override
    public boolean canHandle(IResource resource) {
        if (resource instanceof Observation) {
            List<CodingDt> codings = ((Observation) resource).getCode().getCoding();
            for (CodingDt coding : codings) {
                if (datasenseProperties.getDeathCodes().contains(coding.getCode())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void process(IResource resource, EncounterComposition composition) {
        Observation deathNoteObservation = (Observation) resource;
        PatientDeathDetails patientDeathDetails = new PatientDeathDetails();
        patientDeathDetails.setPatient(composition.getPatientReference().getValue());
        Encounter encounter = composition.getEncounterReference().getValue();
        patientDeathDetails.setEncounter(encounter);
        mapDateOfDeathAndPatientAge(patientDeathDetails, composition, deathNoteObservation, encounter);
        patientDeathDetails.setCircumstancesOfDeath(getCircumstancesOfDeath(composition, deathNoteObservation));
        mapCauseOfDeath(patientDeathDetails, composition, deathNoteObservation);

        patientDeathDetailsDao.save(patientDeathDetails);
    }

    @Override
    public void deleteExisting(EncounterComposition composition) {
        patientDeathDetailsDao.deleteExisting(composition.getPatientReference().getHealthId(), composition.getEncounterReference().getEncounterId());
    }

    private void mapCauseOfDeath(PatientDeathDetails patientDeathDetails, EncounterComposition composition, Observation deathNoteObservation) {
        Observation causeOfDeathObservation = findObservation(composition, deathNoteObservation, datasenseProperties.getCauseOfDeath());
        if (causeOfDeathObservation != null && causeOfDeathObservation.getValue() != null) {
            CodeableConceptDt codeableConcept = (CodeableConceptDt) causeOfDeathObservation.getValue();
            for (CodingDt code : codeableConcept.getCoding()) {
                if (isConceptUrl(code.getSystem())) {
                    patientDeathDetails.setCauseOfDeathConceptUuid(code.getCode());
                } else if (isReferenceTermUrl(code.getSystem())) {
                    patientDeathDetails.setCauseOfDeathCode(code.getCode());
                }
            }
        }
    }

    private String getCircumstancesOfDeath(EncounterComposition composition, Observation deathNoteObservation) {
        Observation circumstancesOfDeathObservation = findObservation(composition, deathNoteObservation, datasenseProperties.getCircumstancesOfDeathUuid());
        if (circumstancesOfDeathObservation != null && circumstancesOfDeathObservation.getValue() != null) {
            String observationValue = observationValueMapper.getObservationValue(circumstancesOfDeathObservation.getValue());
            return observationValue != null ? observationValue.substring(0, Math.min(observationValue.length(), 500)) : null;
        }
        return null;
    }

    private void mapDateOfDeathAndPatientAge(PatientDeathDetails patientDeathDetails, EncounterComposition composition, Observation deathNoteObservation, Encounter encounter) {
        Observation dateOfDeathObs = findObservation(composition, deathNoteObservation, datasenseProperties.getDateOfDeathUuid());
        Date dateOfDeath = getDateValue(encounter, dateOfDeathObs);
        patientDeathDetails.setDateOfDeath(dateOfDeath);
    }

    private Date getDateValue(Encounter encounter, Observation dateOfDeathObs) {
        if (dateOfDeathObs != null && dateOfDeathObs.getValue() != null) {
            String dateOfDeath = observationValueMapper.getObservationValue(dateOfDeathObs.getValue());
            return dateOfDeath != null ? DateUtil.parseDate(dateOfDeath) : null;
        }
        return encounter.getEncounterDateTime();
    }

    private Observation findObservation(EncounterComposition composition, Observation deathNoteObservation, String conceptUuid) {
        for (Observation.Related observationRelatedComponent : deathNoteObservation.getRelated()) {
            Observation childObservation = (Observation) composition.getContext().getResourceForReference(observationRelatedComponent.getTarget());
            if (isConcept(childObservation.getCode(), conceptUuid)) {
                return childObservation;
            }
        }
        return null;
    }

    private boolean isConcept(CodeableConceptDt nameConcepts, String conceptUuid) {
        List<CodingDt> codings = nameConcepts.getCoding();
        for (CodingDt code : codings) {
            if (isConceptUrl(code.getSystem())) {
                if (conceptUuid.equals(code.getCode())) {
                    return true;
                }
            }
        }
        return false;
    }
}
