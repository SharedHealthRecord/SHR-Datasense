package org.sharedhealth.datasense.handler;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.BoundCodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.valueset.ConditionCategoryCodesEnum;
import org.sharedhealth.datasense.model.Diagnosis;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.DiagnosisDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static org.sharedhealth.datasense.util.TrUrl.isConceptUrl;
import static org.sharedhealth.datasense.util.TrUrl.isReferenceTermUrl;

@Component
public class DiagnosisResourceHandler implements FhirResourceHandler {

    private DiagnosisDao diagnosisDao;

    @Autowired
    public DiagnosisResourceHandler(DiagnosisDao diagnosisDao) {
        this.diagnosisDao = diagnosisDao;
    }

    @Override
    public boolean canHandle(IResource resource) {
        if (resource instanceof Condition) {
            Condition condition = (Condition) resource;
            BoundCodeableConceptDt<ConditionCategoryCodesEnum> category = condition.getCategory();
            for (CodingDt coding : category.getCoding()) {
                if (coding.getCode().equalsIgnoreCase("diagnosis")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void process(IResource resource, EncounterComposition composition) {
        Condition fhirDiagnosis = (Condition) resource;
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setPatient(composition.getPatientReference().getValue());
        diagnosis.setEncounter(composition.getEncounterReference().getValue());
        populateDiagnosisCodes(diagnosis, fhirDiagnosis.getCode().getCoding());
        Date dateAsserted = fhirDiagnosis.getDateRecorded();
        Date date = dateAsserted != null ? dateAsserted : composition.getEncounterReference().getValue().getEncounterDateTime();
        diagnosis.setDiagnosisDateTime(date);
        diagnosis.setDiagnosisStatus(fhirDiagnosis.getClinicalStatus());
        diagnosisDao.save(diagnosis);
    }

    @Override
    public void deleteExisting(EncounterComposition composition) {
        diagnosisDao.delete(composition.getEncounterReference().getEncounterId());
    }

    private void populateDiagnosisCodes(Diagnosis diagnosis, List<CodingDt> coding) {
        for (CodingDt code : coding) {
            if (isConceptUrl(code.getSystem())) {
                diagnosis.setDiagnosisConceptId(code.getCode());
            } else if (isReferenceTermUrl(code.getSystem())) {
                diagnosis.setDiagnosisCode(code.getCode());
            }
        }
    }

}
