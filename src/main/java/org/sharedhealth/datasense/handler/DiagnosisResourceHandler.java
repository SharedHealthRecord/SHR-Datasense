package org.sharedhealth.datasense.handler;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Resource;
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
    public boolean canHandle(Resource resource) {
        if (resource instanceof Condition) {
            final List<Coding> resourceCoding = ((Condition) resource).getCategory().get(0).getCoding();
            if (resourceCoding == null || resourceCoding.isEmpty()) {
                return false;
            }
            return resourceCoding.get(0).getCode().equalsIgnoreCase("diagnosis");
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
        Date dateAsserted = fhirDiagnosis.getAssertedDate();
        Date date = dateAsserted != null ? dateAsserted : composition.getEncounterReference().getValue().getEncounterDateTime();
        diagnosis.setDiagnosisDateTime(date);
        diagnosis.setDiagnosisStatus(fhirDiagnosis.getClinicalStatus().toCode());
        diagnosisDao.save(diagnosis);
    }

    @Override
    public void deleteExisting(EncounterComposition composition) {
        diagnosisDao.delete(composition.getEncounterReference().getEncounterId());
    }

    private void populateDiagnosisCodes(Diagnosis diagnosis, List<Coding> coding) {
        for (Coding code : coding) {
            if (isConceptUrl(code.getSystem())) {
                diagnosis.setDiagnosisConceptId(code.getCode());
            } else if (isReferenceTermUrl(code.getSystem())) {
                diagnosis.setDiagnosisCode(code.getCode());
            }
        }
    }

}
