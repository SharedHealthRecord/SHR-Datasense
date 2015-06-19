package org.sharedhealth.datasense.handler;

import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.Boolean;
import org.sharedhealth.datasense.client.TrWebClient;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.ImmunizationReason;
import org.sharedhealth.datasense.model.Medication;
import org.sharedhealth.datasense.model.MedicationStatus;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.MedicationDao;
import org.sharedhealth.datasense.util.DateUtil;
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
    public boolean canHandle(Resource resource) {
        return resource.getResourceType().equals(ResourceType.Immunization);
    }

    private boolean isVaccinationRefused(Resource resource) {
        Immunization immunization = (Immunization) resource;
        Boolean refusedIndicator = immunization.getRefusedIndicator();
        return refusedIndicator == null ? false : refusedIndicator.getValue();
    }

    @Override
    public void process(Resource resource, EncounterComposition composition) {
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
        Immunization.ImmunizationExplanationComponent explanation = immunization.getExplanation();
        String encounterId = composition.getEncounterReference().getValue().getEncounterId();
        String hid = composition.getPatientReference().getValue().getHid();
        if (explanation != null) {
            List<ImmunizationReason> immunizationReasons = new ArrayList<ImmunizationReason>();
            List<CodeableConcept> reasons = explanation.getReason();
            for (CodeableConcept reason : reasons) {
                List<Coding> codings = reason.getCoding();
                for (Coding coding : codings) {
                    ImmunizationReason immunizationReason = new ImmunizationReason();
                    immunizationReason.setCode(coding.getCodeSimple());
                    immunizationReason.setDescr(coding.getDisplaySimple());
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
        medicationDao.deleteExisting(composition.getPatientReference().getHealthId(), composition.getEncounterReference().getEncounterId());
    }

    private void setMedicationCodes(Immunization immunization, Medication medication) {
        List<Coding> codings = immunization.getVaccineType().getCoding();
        for (Coding coding : codings) {
            String system = coding.getSystemSimple();
            if (system != null && isTrMedicationUrl(system)) {
                medication.setDrugId(coding.getCodeSimple());
                break;
            }
        }
    }

    private Date getDateTime(Immunization immunization, Encounter encounter) {
        DateAndTime dateSimple = immunization.getDateSimple();
        return dateSimple != null ? DateUtil.parseDate(dateSimple.toString()) : encounter.getEncounterDateTime();
    }
}
