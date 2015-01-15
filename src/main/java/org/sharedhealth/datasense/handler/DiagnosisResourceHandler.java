package org.sharedhealth.datasense.handler;

import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.sharedhealth.datasense.model.Diagnosis;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.DiagnosisDao;
import org.sharedhealth.datasense.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static org.sharedhealth.datasense.util.TrUrlMatcher.isConceptUrl;
import static org.sharedhealth.datasense.util.TrUrlMatcher.isReferenceTermUrl;

@Component
public class DiagnosisResourceHandler implements FhirResourceHandler {

    private DiagnosisDao diagnosisDao;

    @Autowired
    public DiagnosisResourceHandler(DiagnosisDao diagnosisDao) {
        this.diagnosisDao = diagnosisDao;
    }

    @Override
    public boolean canHandle(Resource resource) {
        if (resource.getResourceType().equals(ResourceType.Condition)) {
            Condition condition = (Condition) resource;
            for (Coding coding : condition.getCategory().getCoding()) {
                if (coding.getDisplaySimple().equalsIgnoreCase("Diagnosis")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void process(Resource resource, EncounterComposition composition) {
        Condition fhirDiagnosis = (Condition) resource;
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setPatient(composition.getPatientReference().getValue());
        diagnosis.setEncounter(composition.getEncounterReference().getValue());
        populateDiagnosisCodes(diagnosis, fhirDiagnosis.getCode().getCoding());
        DateAndTime dateAsserted = fhirDiagnosis.getDateAssertedSimple();
        Date date = dateAsserted != null ? DateUtil.parseDate(dateAsserted.toString()) :
                composition.getEncounterReference().getValue().getEncounterDateTime();
        diagnosis.setDiagnosisDateTime(date);
        diagnosis.setDiagnosisStatus(fhirDiagnosis.getStatus().getValue().toCode());
        diagnosisDao.save(diagnosis);
    }

    private void populateDiagnosisCodes(Diagnosis diagnosis, List<Coding> coding) {
        for (Coding code : coding) {
            if (isConceptUrl(code.getSystemSimple())) {
                diagnosis.setDiagnosisConceptId(code.getCodeSimple());
            } else if (isReferenceTermUrl(code.getSystemSimple())) {
                diagnosis.setDiagnosisCode(code.getCodeSimple());
            }
        }
    }
}
