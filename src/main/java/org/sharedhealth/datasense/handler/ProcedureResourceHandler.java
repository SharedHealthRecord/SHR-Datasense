package org.sharedhealth.datasense.handler;

import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticReport;
import ca.uhn.fhir.model.dstu2.resource.Procedure;
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
    public boolean canHandle(IResource resource) {
        return resource instanceof Procedure;
    }

    @Override
    public void process(IResource resource, EncounterComposition composition) {
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
        List<ResourceReferenceDt> diagnosisReportReferences = procedureResource.getReport();
        if (diagnosisReportReferences.size() == 0) {
            return;
        }
        ResourceReferenceDt resourceReference = diagnosisReportReferences.get(0);
        IResource report = composition.getContext().getResourceForReference(resourceReference);
        if (report == null || !(report instanceof DiagnosticReport)) {
            return;
        }

        DiagnosticReport diagnosticReport = (DiagnosticReport) report;
        List<CodeableConceptDt> codeableConcepts = diagnosticReport.getCodedDiagnosis();
        if (codeableConcepts.size() == 0 ) {
            return;
        }
        CodeableConceptDt codeableConcept = codeableConcepts.get(0);
        List<CodingDt> codings = codeableConcept.getCoding();
        boolean isUuidSet = false;
        boolean isCodeSet = false;
        for (CodingDt code : codings) {
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
        CodeableConceptDt codeableConcept = procedureResource.getCode();
        if (codeableConcept == null) {
            return;
        }
        List<CodingDt> codings = codeableConcept.getCoding();
        boolean isUuidSet = false;
        boolean isCodeSet = false;
        for (CodingDt code : codings) {
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
        IDatatype performed = procedureResource.getPerformed();
        if (performed instanceof PeriodDt) {
            PeriodDt period = (PeriodDt) performed;
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
