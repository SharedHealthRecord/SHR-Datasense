package org.sharedhealth.datasense.handler;

import ca.uhn.fhir.model.api.IDatatype;
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
    public boolean canHandle(IResource resource) {
        if (resource instanceof Observation) {
            List<CodingDt> codings = ((Observation) resource).getCode().getCoding();
            for (CodingDt coding : codings) {
                if (configurationService.getDeathCodes().contains(coding.getCode())) {
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
        mapCircumstanceOfDeath(patientDeathDetails, composition, deathNoteObservation);
        mapCauseOfDeath(patientDeathDetails, composition, deathNoteObservation);
        mapPlaceOfDeath(patientDeathDetails, composition, deathNoteObservation);
        patientDeathDetailsDao.save(patientDeathDetails);
    }

    @Override
    public void deleteExisting(EncounterComposition composition) {
        patientDeathDetailsDao.deleteExisting(composition.getPatientReference().getHealthId(), composition.getEncounterReference().getEncounterId());
    }

    private void mapCauseOfDeath(PatientDeathDetails patientDeathDetails, EncounterComposition composition, Observation deathNoteObservation) {
        CodeableConceptDt causeOfDeathConcept = getCodeableConceptValue(findObservation(composition, deathNoteObservation, configurationService.getCauseOfDeath()));
        if (causeOfDeathConcept != null) {
            for (CodingDt code : causeOfDeathConcept.getCoding()) {
                if (isConceptUrl(code.getSystem())) {
                    patientDeathDetails.setCauseOfDeathConceptUuid(code.getCode());
                } else if (isReferenceTermUrl(code.getSystem())) {
                    patientDeathDetails.setCauseOfDeathCode(code.getCode());
                }
            }
        }
    }

    private CodeableConceptDt getCodeableConceptValue(Observation observation) {
        if (observation == null) return null;
        IDatatype value = observation.getValue();
        return (value instanceof CodeableConceptDt) ? (CodeableConceptDt) value : null;
    }

    private void mapCircumstanceOfDeath(PatientDeathDetails patientDeathDetails, EncounterComposition composition, Observation deathNoteObservation) {
        CodeableConceptDt circumstancesOfDeathConcept = getCodeableConceptValue(findObservation(composition, deathNoteObservation, configurationService.getCircumstancesOfDeathUuid()));
        if (circumstancesOfDeathConcept != null) {
            for (CodingDt code : circumstancesOfDeathConcept.getCoding()) {
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
        CodeableConceptDt placeOfDeathConcept = getCodeableConceptValue(findObservation(composition, deathNoteObservation, configurationService.getPlaceOfDeathConceptUuid()));
        if (placeOfDeathConcept != null) {
            for (CodingDt code : placeOfDeathConcept.getCoding()) {
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
