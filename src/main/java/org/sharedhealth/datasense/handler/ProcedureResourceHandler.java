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
        Procedure procedureResource = (Procedure) resource;
        org.sharedhealth.datasense.model.Procedure procedure = new org.sharedhealth.datasense.model.Procedure();
        procedure.setPatientHid(composition.getPatientReference().getValue().getHid());
        procedure.setEncounterId(composition.getEncounterReference().getValue().getEncounterId());
        procedure.setEncounterDate(composition.getEncounterReference().getValue().getEncounterDateTime());
        setStartAndEndDate(procedure, procedureResource);
        setProcedureType(procedure, procedureResource);
        setProcedureDiagnosis(procedure, procedureResource, composition);

        procedureDao.save(procedure);
    }

    @Override
    public void deleteExisting(EncounterComposition composition) {
        procedureDao.deleteExisting(composition.getPatientReference().getHealthId(), composition.getEncounterReference().getEncounterId());
    }

    private void setProcedureDiagnosis(org.sharedhealth.datasense.model.Procedure procedure,
                                       Procedure procedureResource, EncounterComposition composition) {
        List<ResourceReference> diagnosisReportReference = procedureResource.getReport();
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
                procedure.setDiagnosisUuid(code.getCodeSimple());
                isUuidSet = true;
            } else if (isReferenceTermUrl(code.getSystemSimple())) {
                procedure.setDiagnosisCode(code.getCodeSimple());
                isCodeSet = true;
            }
        }
    }

    private void setProcedureType(org.sharedhealth.datasense.model.Procedure procedure, Procedure procedureResource) {
        CodeableConcept codeableConcept = procedureResource.getType();
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
                procedure.setProcedureUuid(code.getCodeSimple());
                isUuidSet = true;
            } else if (isReferenceTermUrl(code.getSystemSimple())) {
                procedure.setProcedureCode(code.getCodeSimple());
                isCodeSet = true;
            }
        }
    }

    private void setStartAndEndDate(org.sharedhealth.datasense.model.Procedure procedure, Procedure procedureResource) {
        Period period = procedureResource.getDate();
        if (period != null) {
            String startDate = observationValueMapper.getObservationValue(period.getStart());
            if (startDate != null) {
                procedure.setStartDate(DateUtil.parseDate(startDate));
            }
            String endDate = observationValueMapper.getObservationValue(period.getEnd());
            if (endDate != null) {
                procedure.setEndDate(DateUtil.parseDate(endDate));
            }
        }
    }


}
