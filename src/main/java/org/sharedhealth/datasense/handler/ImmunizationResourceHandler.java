package org.sharedhealth.datasense.handler;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Resource;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.ImmunizationReason;
import org.sharedhealth.datasense.model.Immunization;
import org.sharedhealth.datasense.model.MedicationStatus;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.ImmunizationDao;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.sharedhealth.datasense.util.TrUrl.isTrMedicationUrl;

@Component
public class ImmunizationResourceHandler implements FhirResourceHandler {

    @Autowired
    private ImmunizationDao immunizationDao;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ImmunizationResourceHandler.class);

    @Override
    public boolean canHandle(Resource resource) {
        return resource instanceof org.hl7.fhir.dstu3.model.Immunization;
    }

    private boolean isVaccinationRefused(Resource resource) {
        org.hl7.fhir.dstu3.model.Immunization immunization = (org.hl7.fhir.dstu3.model.Immunization) resource;
        Boolean given = immunization.getNotGiven();
        return given == null ? false : given;
    }

    @Override
    public void process(Resource resource, EncounterComposition composition) {
        if (isVaccinationRefused(resource)) {
            return;
        }
        org.hl7.fhir.dstu3.model.Immunization fhirImmunization = (org.hl7.fhir.dstu3.model.Immunization) resource;
        Immunization immunization = new Immunization();
        immunization.setStatus(MedicationStatus.ImmunizationAdministered);
        Encounter encounter = composition.getEncounterReference().getValue();
        immunization.setEncounter(encounter);
        immunization.setPatient(composition.getPatientReference().getValue());
        immunization.setDateTime(getDateTime(fhirImmunization, encounter));
        setImmunizationCodes(fhirImmunization, immunization);
        if (immunization.getDrugId() == null) {
            logger.warn("Cannot save non-coded immunizations.");
            return;
        }
        immunizationDao.save(immunization);
        processImmunizationReason(fhirImmunization, immunization, composition);
    }

    private void processImmunizationReason(org.hl7.fhir.dstu3.model.Immunization fhirImmunization, Immunization immunization, EncounterComposition composition) {
        org.hl7.fhir.dstu3.model.Immunization.ImmunizationExplanationComponent explanation = fhirImmunization.getExplanation();
        String encounterId = composition.getEncounterReference().getValue().getEncounterId();
        String hid = composition.getPatientReference().getValue().getHid();
        if (explanation != null) {
            List<ImmunizationReason> immunizationReasons = new ArrayList<ImmunizationReason>();
            List<CodeableConcept> reasons = explanation.getReason();
            for (CodeableConcept reason : reasons) {
                List<Coding> codings = reason.getCoding();
                for (Coding coding : codings) {
                    ImmunizationReason immunizationReason = new ImmunizationReason();
                    immunizationReason.setCode(coding.getCode());
                    immunizationReason.setDescr(coding.getDisplay());
                    immunizationReason.setIncidentUuid(immunization.getUuid());
                    immunizationReason.setEncounterId(encounterId);
                    immunizationReason.setHid(hid);
                    immunizationReasons.add(immunizationReason);
                }
            }
            immunizationDao.save(immunizationReasons);
        }
    }

    @Override
    public void deleteExisting(EncounterComposition composition) {
        String encounterId = composition.getEncounterReference().getEncounterId();
        immunizationDao.deleteExisting(encounterId);
        immunizationDao.deleteExistingImmunizationReasons(encounterId);
    }

    private void setImmunizationCodes(org.hl7.fhir.dstu3.model.Immunization fhirImmunization, Immunization immunization) {
        List<Coding> codings = fhirImmunization.getVaccineCode().getCoding();
        for (Coding coding : codings) {
            String system = coding.getSystem();
            if (system != null && isTrMedicationUrl(system)) {
                immunization.setDrugId(coding.getCode());
                break;
            }
        }
    }

    private Date getDateTime(org.hl7.fhir.dstu3.model.Immunization fhirImmunization, Encounter encounter) {
        Date date = fhirImmunization.getDate();
        return date != null ? date : encounter.getEncounterDateTime();
    }
}
