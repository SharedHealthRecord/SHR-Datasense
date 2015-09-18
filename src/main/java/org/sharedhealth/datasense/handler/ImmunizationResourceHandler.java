package org.sharedhealth.datasense.handler;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.BoundCodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.resource.Immunization;
import ca.uhn.fhir.model.dstu2.valueset.ImmunizationReasonCodesEnum;
import org.sharedhealth.datasense.client.TrWebClient;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.ImmunizationReason;
import org.sharedhealth.datasense.model.Medication;
import org.sharedhealth.datasense.model.MedicationStatus;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.MedicationDao;
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
    private MedicationDao medicationDao;
    @Autowired
    private TrWebClient trWebClient;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ImmunizationResourceHandler.class);

    @Override
    public boolean canHandle(IResource resource) {
        return resource instanceof Immunization;
    }

    private boolean isVaccinationRefused(IResource resource) {
        Immunization immunization = (Immunization) resource;
        Boolean given = immunization.getWasNotGiven();
        return given == null ? false : given;
    }

    @Override
    public void process(IResource resource, EncounterComposition composition) {
        if (isVaccinationRefused(resource)) {
            return;
        }
        Immunization immunization = (Immunization) resource;
        Medication medication = new Medication();
        medication.setStatus(MedicationStatus.ImmunizationAdministered);
        Encounter encounter = composition.getEncounterReference().getValue();
        medication.setEncounter(encounter);
        medication.setPatient(composition.getPatientReference().getValue());
        medication.setDateTime(getDateTime(immunization, encounter));
        setMedicationCodes(immunization, medication);
        if (medication.getDrugId() == null) {
            logger.warn("Cannot save non-coded immunizations.");
            return;
        }
        medicationDao.save(medication);
        processImmunizationReason(immunization, medication, composition);
    }

    private void processImmunizationReason(Immunization immunization, Medication medication, EncounterComposition composition) {
        Immunization.Explanation explanation = immunization.getExplanation();
        String encounterId = composition.getEncounterReference().getValue().getEncounterId();
        String hid = composition.getPatientReference().getValue().getHid();
        if (explanation != null) {
            List<ImmunizationReason> immunizationReasons = new ArrayList<ImmunizationReason>();
            List<BoundCodeableConceptDt<ImmunizationReasonCodesEnum>> reasons = explanation.getReason();
            for (BoundCodeableConceptDt<ImmunizationReasonCodesEnum> reason : reasons) {
                List<CodingDt> codings = reason.getCoding();
                for (CodingDt coding : codings) {
                    ImmunizationReason immunizationReason = new ImmunizationReason();
                    immunizationReason.setCode(coding.getCode());
                    immunizationReason.setDescr(coding.getDisplay());
                    immunizationReason.setIncidentUuid(medication.getUuid());
                    immunizationReason.setEncounterId(encounterId);
                    immunizationReason.setHid(hid);
                    immunizationReasons.add(immunizationReason);
                }
            }
            medicationDao.save(immunizationReasons);
        }
    }

    @Override
    public void deleteExisting(EncounterComposition composition) {
        String healthId = composition.getPatientReference().getHealthId();
        String encounterId = composition.getEncounterReference().getEncounterId();
        medicationDao.deleteExisting(healthId, encounterId);
        medicationDao.deleteExistingImmunizationReasons(healthId, encounterId);
    }

    private void setMedicationCodes(Immunization immunization, Medication medication) {
        List<CodingDt> codings = immunization.getVaccineCode().getCoding();
        for (CodingDt coding : codings) {
            String system = coding.getSystem();
            if (system != null && isTrMedicationUrl(system)) {
                medication.setDrugId(coding.getCode());
                break;
            }
        }
    }

    private Date getDateTime(Immunization immunization, Encounter encounter) {
        Date date = immunization.getDate();
        return date != null ? date : encounter.getEncounterDateTime();
    }
}
