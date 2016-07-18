package org.sharedhealth.datasense.handler;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
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
    public boolean canHandle(IResource resource) {
        return resource instanceof ca.uhn.fhir.model.dstu2.resource.Immunization;
    }

    private boolean isVaccinationRefused(IResource resource) {
        ca.uhn.fhir.model.dstu2.resource.Immunization immunization = (ca.uhn.fhir.model.dstu2.resource.Immunization) resource;
        Boolean given = immunization.getWasNotGiven();
        return given == null ? false : given;
    }

    @Override
    public void process(IResource resource, EncounterComposition composition) {
        if (isVaccinationRefused(resource)) {
            return;
        }
        ca.uhn.fhir.model.dstu2.resource.Immunization fhirImmunization = (ca.uhn.fhir.model.dstu2.resource.Immunization) resource;
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

    private void processImmunizationReason(ca.uhn.fhir.model.dstu2.resource.Immunization fhirImmunization, Immunization immunization, EncounterComposition composition) {
        ca.uhn.fhir.model.dstu2.resource.Immunization.Explanation explanation = fhirImmunization.getExplanation();
        String encounterId = composition.getEncounterReference().getValue().getEncounterId();
        String hid = composition.getPatientReference().getValue().getHid();
        if (explanation != null) {
            List<ImmunizationReason> immunizationReasons = new ArrayList<ImmunizationReason>();
            List<CodeableConceptDt> reasons = explanation.getReason();
            for (CodeableConceptDt reason : reasons) {
                List<CodingDt> codings = reason.getCoding();
                for (CodingDt coding : codings) {
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
        String healthId = composition.getPatientReference().getHealthId();
        String encounterId = composition.getEncounterReference().getEncounterId();
        immunizationDao.deleteExisting(healthId, encounterId);
        immunizationDao.deleteExistingImmunizationReasons(healthId, encounterId);
    }

    private void setImmunizationCodes(ca.uhn.fhir.model.dstu2.resource.Immunization fhirImmunization, Immunization immunization) {
        List<CodingDt> codings = fhirImmunization.getVaccineCode().getCoding();
        for (CodingDt coding : codings) {
            String system = coding.getSystem();
            if (system != null && isTrMedicationUrl(system)) {
                immunization.setDrugId(coding.getCode());
                break;
            }
        }
    }

    private Date getDateTime(ca.uhn.fhir.model.dstu2.resource.Immunization fhirImmunization, Encounter encounter) {
        Date date = fhirImmunization.getDate();
        return date != null ? date : encounter.getEncounterDateTime();
    }
}
