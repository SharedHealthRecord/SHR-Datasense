package org.sharedhealth.datasense.handler;

import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Resource;
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
    public boolean canHandle(Resource resource) {
        if (resource instanceof org.hl7.fhir.instance.model.Observation) {
            List<Coding> codings = ((Observation) resource).getName().getCoding();
            for (Coding coding : codings) {
                if (datasenseProperties.getDeathCodes().contains(coding.getCodeSimple())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void process(Resource resource, EncounterComposition composition) {
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
            CodeableConcept codeableConcept = (CodeableConcept) causeOfDeathObservation.getValue();
            for (Coding code : codeableConcept.getCoding()) {
                if (isConceptUrl(code.getSystemSimple())) {
                    patientDeathDetails.setCauseOfDeathConceptUuid(code.getCodeSimple());
                } else if (isReferenceTermUrl(code.getSystemSimple())) {
                    patientDeathDetails.setCauseOfDeathCode(code.getCodeSimple());
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
        for (Observation.ObservationRelatedComponent observationRelatedComponent : deathNoteObservation.getRelated()) {
            Observation childObservation = (Observation) composition.getContext().getResourceByReferenceFromFeed(observationRelatedComponent.getTarget());
            if (isConcept(childObservation.getName(), conceptUuid)) {
                return childObservation;
            }
        }
        return null;
    }

    private boolean isConcept(CodeableConcept nameConcepts, String conceptUuid) {
        List<Coding> codings = nameConcepts.getCoding();
        for (Coding code : codings) {
            if (isConceptUrl(code.getSystemSimple())) {
                if (conceptUuid.equals(code.getCodeSimple())) {
                    return true;
                }
            }
        }
        return false;
    }
}
