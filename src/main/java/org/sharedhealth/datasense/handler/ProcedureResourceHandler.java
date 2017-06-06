package org.sharedhealth.datasense.handler;

import org.hl7.fhir.dstu3.model.*;
import org.sharedhealth.datasense.handler.mappers.ObservationValueMapper;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.ProcedureDao;
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
        return resource instanceof Procedure;
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
        procedureDao.deleteExisting(composition.getEncounterReference().getEncounterId());
    }

    private void setProcedureDiagnosis(org.sharedhealth.datasense.model.Procedure procedure,
                                       Procedure procedureResource, EncounterComposition composition) {
        List<Reference> diagnosisReportReferences = procedureResource.getReport();
        if (diagnosisReportReferences.size() == 0) {
            return;
        }
        Reference resourceReference = diagnosisReportReferences.get(0);
        Resource report = composition.getContext().getResourceForReference(resourceReference);
        if (report == null || !(report instanceof DiagnosticReport)) {
            return;
        }

        DiagnosticReport diagnosticReport = (DiagnosticReport) report;
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
            if (isConceptUrl(code.getSystem())) {
                procedure.setDiagnosisUuid(code.getCode());
                isUuidSet = true;
            } else if (isReferenceTermUrl(code.getSystem())) {
                procedure.setDiagnosisCode(code.getCode());
                isCodeSet = true;
            }
        }
    }

    private void setProcedureType(org.sharedhealth.datasense.model.Procedure procedure, Procedure procedureResource) {
        CodeableConcept codeableConcept = procedureResource.getCode();
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
            if (isConceptUrl(code.getSystem())) {
                procedure.setProcedureUuid(code.getCode());
                isUuidSet = true;
            } else if (isReferenceTermUrl(code.getSystem())) {
                procedure.setProcedureCode(code.getCode());
                isCodeSet = true;
            }
        }
    }

    private void setStartAndEndDate(org.sharedhealth.datasense.model.Procedure procedure, Procedure procedureResource) {
        Type performed = procedureResource.getPerformed();
        if (performed instanceof Period) {
            Period period = (Period) performed;
            if (period != null) {
                if (period.getStart() != null) {
                    procedure.setStartDate(period.getStart());
                }
                if (period.getEnd() != null) {
                    procedure.setEndDate(period.getEnd());
                }
            }
        }
    }


}
