package org.sharedhealth.datasense.handler;

import org.hl7.fhir.instance.model.*;
import org.sharedhealth.datasense.handler.mappers.ObservationValueMapper;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.ProcedureDao;
import org.sharedhealth.datasense.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.sharedhealth.datasense.util.TrUrl.isConceptUrl;
import static org.sharedhealth.datasense.util.TrUrl.isReferenceTermUrl;

@Component
public class ProcedureResourceHandler implements FhirResourceHandler {


    @Autowired
    private ProcedureDao procedureDao;

    private ObservationValueMapper observationValueMapper;

    @Override
    public boolean canHandle(Resource resource) {
        return resource.getResourceType().equals(ResourceType.Procedure);
    }

    @Override
    public void process(Resource resource, EncounterComposition composition) {
        observationValueMapper = new ObservationValueMapper();
        Procedure procedure = (Procedure) resource;
        org.sharedhealth.datasense.model.Procedure dataSenseProcedure = new org.sharedhealth.datasense.model.Procedure();
        dataSenseProcedure.setPatientHid(composition.getPatientReference().getValue().getHid());
        dataSenseProcedure.setEncounterId(composition.getEncounterReference().getValue().getEncounterId());
        dataSenseProcedure.setEncounterDate(composition.getEncounterReference().getValue().getEncounterDateTime());
        setStartAndEndDate(dataSenseProcedure, procedure);
        setProcedureType(dataSenseProcedure, procedure);
        setProcedureDiagnosis(dataSenseProcedure, procedure, composition);

        procedureDao.save(dataSenseProcedure);
    }

    private void setProcedureDiagnosis(org.sharedhealth.datasense.model.Procedure dataSenseProcedure,
                                       Procedure procedure, EncounterComposition composition) {
        List<ResourceReference> diagnosisReportReference = procedure.getReport();
        if (diagnosisReportReference.size() == 0) {
            return;
        }
        ResourceReference resourceReference = diagnosisReportReference.get(0);
        Resource resource = composition.getContext().getResourceByReferenceFromFeed(resourceReference);
        if (resource == null || !(resource instanceof DiagnosticReport)) {
            return;
        }

        DiagnosticReport diagnosticReport = (DiagnosticReport) resource;
        List<CodeableConcept> codeableConcepts = diagnosticReport.getCodedDiagnosis();
        if (codeableConcepts.size() == 0 ) {
            return;
        }
        CodeableConcept codeableConcept = codeableConcepts.get(0);
        List<Coding> codings = codeableConcept.getCoding();
        boolean isUuidSet = false;
        boolean isCodeSet = false;
        for (Coding code : codings) {
            if (isUuidSet && isCodeSet) {
                break;
            }
            if (isConceptUrl(code.getSystemSimple())) {
                dataSenseProcedure.setDiagnosisUuid(code.getCodeSimple());
                isUuidSet = true;
            } else if (isReferenceTermUrl(code.getSystemSimple())) {
                dataSenseProcedure.setDiagnosisCode(code.getCodeSimple());
                isCodeSet = true;
            }
        }
    }

    private void setProcedureType(org.sharedhealth.datasense.model.Procedure dataSenseProcedure, Procedure procedure) {
        CodeableConcept codeableConcept = procedure.getType();
        if (codeableConcept == null) {
            return;
        }
        List<Coding> codings = codeableConcept.getCoding();
        boolean isUuidSet = false;
        boolean isCodeSet = false;
        for (Coding code : codings) {
            if (isUuidSet && isCodeSet) {
                break;
            }
            if (isConceptUrl(code.getSystemSimple())) {
                dataSenseProcedure.setProcedureUuid(code.getCodeSimple());
                isUuidSet = true;
            } else if (isReferenceTermUrl(code.getSystemSimple())) {
                dataSenseProcedure.setProcedureCode(code.getCodeSimple());
                isCodeSet = true;
            }
        }
    }

    private void setStartAndEndDate(org.sharedhealth.datasense.model.Procedure dataSenseProcedure, Procedure procedure) {
        Period period = procedure.getDate();
        if (period != null) {
            String startDate = observationValueMapper.getObservationValue(period.getStart());
            if (startDate != null) {
                dataSenseProcedure.setStartDate(DateUtil.parseDate(startDate));
            }
            String endDate = observationValueMapper.getObservationValue(period.getEnd());
            if (endDate != null) {
                dataSenseProcedure.setEndDate(DateUtil.parseDate(endDate));
            }
        }
    }


}
