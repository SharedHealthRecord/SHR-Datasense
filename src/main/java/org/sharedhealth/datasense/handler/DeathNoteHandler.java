package org.sharedhealth.datasense.handler;

import org.hl7.fhir.dstu3.model.*;
import org.sharedhealth.datasense.handler.mappers.ObservationValueMapper;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.PatientDeathDetails;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.PatientDeathDetailsDao;
import org.sharedhealth.datasense.service.ConfigurationService;
import org.sharedhealth.datasense.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static org.sharedhealth.datasense.util.TrUrl.isConceptUrl;
import static org.sharedhealth.datasense.util.TrUrl.isReferenceTermUrl;

@Component
public class DeathNoteHandler implements FhirResourceHandler {

    private PatientDeathDetailsDao patientDeathDetailsDao;
    private ConfigurationService configurationService;
    private ObservationValueMapper observationValueMapper;


    @Autowired
    public DeathNoteHandler(PatientDeathDetailsDao patientDeathDetailsDao, ConfigurationService configurationService) {
        this.patientDeathDetailsDao = patientDeathDetailsDao;
        this.configurationService = configurationService;
        this.observationValueMapper = new ObservationValueMapper();
    }

    @Override
    public boolean canHandle(Resource resource) {
        if (resource instanceof Observation) {
            List<Coding> codings = ((Observation) resource).getCode().getCoding();
            for (Coding coding : codings) {
                if (configurationService.getDeathCodes().contains(coding.getCode())) {
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
        mapCircumstanceOfDeath(patientDeathDetails, composition, deathNoteObservation);
        mapCauseOfDeath(patientDeathDetails, composition, deathNoteObservation);
        mapPlaceOfDeath(patientDeathDetails, composition, deathNoteObservation);
        patientDeathDetailsDao.save(patientDeathDetails);
    }

    @Override
    public void deleteExisting(EncounterComposition composition) {
        patientDeathDetailsDao.deleteExisting(composition.getEncounterReference().getEncounterId());
    }

    private void mapCauseOfDeath(PatientDeathDetails patientDeathDetails, EncounterComposition composition, Observation deathNoteObservation) {
        CodeableConcept causeOfDeathConcept = getCodeableConceptValue(findObservation(composition, deathNoteObservation, configurationService.getCauseOfDeath()));
        if (causeOfDeathConcept != null) {
            for (Coding code : causeOfDeathConcept.getCoding()) {
                if (isConceptUrl(code.getSystem())) {
                    patientDeathDetails.setCauseOfDeathConceptUuid(code.getCode());
                } else if (isReferenceTermUrl(code.getSystem())) {
                    patientDeathDetails.setCauseOfDeathCode(code.getCode());
                }
            }
        }
    }

    private CodeableConcept getCodeableConceptValue(Observation observation) {
        if (observation == null) return null;
        Type value = observation.getValue();
        return (value instanceof CodeableConcept) ? (CodeableConcept) value : null;
    }

    private void mapCircumstanceOfDeath(PatientDeathDetails patientDeathDetails, EncounterComposition composition, Observation deathNoteObservation) {
        CodeableConcept circumstancesOfDeathConcept = getCodeableConceptValue(findObservation(composition, deathNoteObservation, configurationService.getCircumstancesOfDeathUuid()));
        if (circumstancesOfDeathConcept != null) {
            for (Coding code : circumstancesOfDeathConcept.getCoding()) {
                if (isConceptUrl(code.getSystem())) {
                    patientDeathDetails.setCircumstancesOfDeathUuid(code.getCode());
                } else if (isReferenceTermUrl(code.getSystem())) {
                    patientDeathDetails.setCircumstancesOfDeathCode(code.getCode());
                }
            }
        }
        //patientDeathDetails.setCircumstancesOfDeath(getCircumstancesOfDeath(composition, deathNoteObservation));
    }

    private void mapPlaceOfDeath(PatientDeathDetails patientDeathDetails, EncounterComposition composition, Observation deathNoteObservation) {
        CodeableConcept placeOfDeathConcept = getCodeableConceptValue(findObservation(composition, deathNoteObservation, configurationService.getPlaceOfDeathConceptUuid()));
        if (placeOfDeathConcept != null) {
            for (Coding code : placeOfDeathConcept.getCoding()) {
                if (isConceptUrl(code.getSystem())) {
                    patientDeathDetails.setPlaceOfDeathUuid(code.getCode());
                } else if (isReferenceTermUrl(code.getSystem())) {
                    patientDeathDetails.setPlaceOfDeathCode(code.getCode());
                }
            }
        }
    }


    private void mapDateOfDeathAndPatientAge(PatientDeathDetails patientDeathDetails, EncounterComposition composition, Observation deathNoteObservation, Encounter encounter) {
        Observation dateOfDeathObs = findObservation(composition, deathNoteObservation, configurationService.getDateOfDeathUuid());
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
            Observation childObservation = (Observation) composition.getContext().getResourceForReference(observationRelatedComponent.getTarget());
            if (isConcept(childObservation.getCode(), conceptUuid)) {
                return childObservation;
            }
        }
        return null;
    }

    private boolean isConcept(CodeableConcept nameConcepts, String conceptUuid) {
        List<Coding> codings = nameConcepts.getCoding();
        for (Coding code : codings) {
            if (isConceptUrl(code.getSystem())) {
                if (conceptUuid.equals(code.getCode())) {
                    return true;
                }
            }
        }
        return false;
    }


}
