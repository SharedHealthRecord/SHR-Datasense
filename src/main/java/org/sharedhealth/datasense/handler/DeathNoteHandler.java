package org.sharedhealth.datasense.handler;

import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.String_;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.PatientDeathDetails;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.ConceptDao;
import org.sharedhealth.datasense.repository.PatientDeathDetailsDao;
import org.sharedhealth.datasense.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static org.sharedhealth.datasense.util.DateUtil.getDays;
import static org.sharedhealth.datasense.util.DateUtil.getMonths;
import static org.sharedhealth.datasense.util.DateUtil.getYears;
import static org.sharedhealth.datasense.util.TrUrl.isConceptUrl;
import static org.sharedhealth.datasense.util.TrUrl.isReferenceTermUrl;

@Component
public class DeathNoteHandler implements FhirResourceHandler {

    private DatasenseProperties datasenseProperties;
    private PatientDeathDetailsDao patientDeathDetailsDao;
    private ConceptDao conceptDao;

    @Autowired
    public DeathNoteHandler(DatasenseProperties datasenseProperties, PatientDeathDetailsDao patientDeathDetailsDao, ConceptDao conceptDao) {
        this.datasenseProperties = datasenseProperties;
        this.patientDeathDetailsDao = patientDeathDetailsDao;
        this.conceptDao = conceptDao;
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

    private void mapCauseOfDeath(PatientDeathDetails patientDeathDetails, EncounterComposition composition, Observation deathNoteObservation) {
        Observation causeOfDeathObservation = findObservation(composition, deathNoteObservation, datasenseProperties.getCauseOfDeath());
        if (causeOfDeathObservation != null) {
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
        return circumstancesOfDeathObservation != null ? ((String_) circumstancesOfDeathObservation.getValue()).getValue() : null;
    }

    private void mapDateOfDeathAndPatientAge(PatientDeathDetails patientDeathDetails, EncounterComposition composition, Observation deathNoteObservation, Encounter encounter) {
        Observation dateOfDeathObs = findObservation(composition, deathNoteObservation, datasenseProperties.getDateOfDeathUuid());
        Date dateOfDeath = getDateValue(encounter, dateOfDeathObs);
        patientDeathDetails.setDateOfDeath(dateOfDeath);
        patientDeathDetails.setPatientAgeInYears(getYears(patientDeathDetails.getPatient().getDateOfBirth(), dateOfDeath));
        patientDeathDetails.setPatientAgeInMonths(getMonths(patientDeathDetails.getPatient().getDateOfBirth(), dateOfDeath));
        patientDeathDetails.setPatientAgeInDays(getDays(patientDeathDetails.getPatient().getDateOfBirth(), dateOfDeath));
    }

    private Date getDateValue(Encounter encounter, Observation dateOfDeathObs) {
        if (dateOfDeathObs != null) {
            if (dateOfDeathObs.getValue() != null && dateOfDeathObs.getValue() instanceof org.hl7.fhir.instance.model.Date) {
                DateAndTime dateAndTime = ((org.hl7.fhir.instance.model.Date) dateOfDeathObs.getValue()).getValue();
                return DateUtil.parseDate(dateAndTime.toString());
            }
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
