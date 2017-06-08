package org.sharedhealth.datasense.handler;


import org.hl7.fhir.dstu3.model.*;
import org.sharedhealth.datasense.model.Observation;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.model.fhir.ProviderReference;
import org.sharedhealth.datasense.repository.ProcedureRequest;
import org.sharedhealth.datasense.repository.DiagnosticReportDao;
import org.sharedhealth.datasense.util.TrUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.sharedhealth.datasense.util.ResourceReferenceUtils.*;
import static org.sharedhealth.datasense.util.TrUrl.isConceptUrl;

@Component
public class DiagnosticReportResourceHandler implements FhirResourceHandler {
    @Autowired
    DiagnosticReportDao diagnosticReportDao;
    @Autowired
    ObservationResourceHandler observationResourceHandler;
    @Autowired
    ProcedureRequest procedureRequest;

    @Override
    public boolean canHandle(Resource resource) {
        return resource instanceof DiagnosticReport;
    }

    @Override
    public void process(Resource resource, EncounterComposition composition) {

        DiagnosticReport fhirDiagnosticReport = (DiagnosticReport) resource;

        org.sharedhealth.datasense.model.DiagnosticReport diagnosticReport = new org.sharedhealth.datasense.model.DiagnosticReport();
        String encounterId = composition.getEncounterReference().getEncounterId();

        diagnosticReport.setEncounterId(encounterId);
        diagnosticReport.setPatientHid(composition.getPatientReference().getHealthId());
        diagnosticReport.setReportDate(fhirDiagnosticReport.getIssued());
        List<DiagnosticReport.DiagnosticReportPerformerComponent> performer = fhirDiagnosticReport.getPerformer();
        diagnosticReport.setFulfiller(ProviderReference.parseUrl(performer.get(0).getActor().getReference()));
        setCategory(fhirDiagnosticReport, diagnosticReport);
        populateOrderCodeAndConcept(fhirDiagnosticReport.getCode().getCoding(), diagnosticReport);

        if (diagnosticReport.getCode() == null && diagnosticReport.getReportConcept() == null) return;
        diagnosticReport.setOrderId(populateShrOrderId(fhirDiagnosticReport));

        int reportId = diagnosticReportDao.save(diagnosticReport);
        saveResultObservations(composition, fhirDiagnosticReport, reportId);

    }

    @Override
    public void deleteExisting(EncounterComposition composition) {
        diagnosticReportDao.deleteExisting(composition.getEncounterReference().getEncounterId());
    }


    private void saveResultObservations(EncounterComposition composition, DiagnosticReport fhirDiagnosticReport, int reportId) {
        List<Reference> resultReferences = fhirDiagnosticReport.getResult();
        for (Reference resultReference : resultReferences) {
            Resource observationForReference = composition.getResourceByReference(resultReference);
            observationResourceHandler.mapObservation(composition, new Observation(), observationForReference, reportId);
        }
    }

    private void setCategory(DiagnosticReport fhirDiagnosticReport, org.sharedhealth.datasense.model.DiagnosticReport diagnosticReport) {
        if (!fhirDiagnosticReport.getCategory().isEmpty())
            diagnosticReport.setReportCategory(fhirDiagnosticReport.getCategory().getCoding().get(0).getCode());
        else
            diagnosticReport.setReportCategory("LAB");
    }

    private void populateOrderCodeAndConcept(List<Coding> coding, org.sharedhealth.datasense.model.DiagnosticReport diagnosticReport) {
        for (Coding codingDt : coding) {
            if (isConceptUrl(codingDt.getSystem())) {
                diagnosticReport.setReportConcept(codingDt.getCode());
            } else if (TrUrl.isReferenceTermUrl(codingDt.getSystem())) {
                diagnosticReport.setCode(codingDt.getCode());
            }
        }
    }

    private Integer populateShrOrderId(DiagnosticReport fhirDiagnosticReport) {
        if (hasShrOrderUuid(fhirDiagnosticReport.getBasedOn()))
            return getConcatenatedShrOrderUuidFromRequest(fhirDiagnosticReport.getBasedOn());
        return getShrOrderIdFromEncounter(fhirDiagnosticReport);
    }

    private boolean hasShrOrderUuid(List<Reference> request) {
        if (null == request) return false;
        String referenceUrl = getReferenceUrlFromResourceReference(request.get(0));
        if (referenceUrl.isEmpty()) return false;
        return referenceUrl.contains("#" + new org.hl7.fhir.dstu3.model.ProcedureRequest().getResourceType().name());
    }

    private Integer getConcatenatedShrOrderUuidFromRequest(List<Reference> request) {
        String referenceUrl = getReferenceUrlFromResourceReference(request.get(0));
        String orderEncounterId = getEncounterUuidFromReferenceUrl(referenceUrl);
        String shrOrderUuid = getOrderUuidFromReferenceUrl(referenceUrl);

        String concatenatedShrOrderUuid = orderEncounterId + ":" + shrOrderUuid;
        return procedureRequest.getOrderId(concatenatedShrOrderUuid);

    }


    private Integer getShrOrderIdFromEncounter(DiagnosticReport fhirDiagnosticReport) {
        String orderEncounterId = getOrderEncounterId(fhirDiagnosticReport);
        for (Coding codingDt : fhirDiagnosticReport.getCode().getCoding()) {
            if (isConceptUrl(codingDt.getSystem())) {
                return procedureRequest.getOrderId(orderEncounterId, codingDt.getCode());
            }
        }
        return null;
    }

    private String getOrderEncounterId(DiagnosticReport fhirDiagnosticReport) {
        List<Reference> request = fhirDiagnosticReport.getBasedOn();
        if (null == request) return null;
        String referenceUrl = getReferenceUrlFromResourceReference(request.get(0));
        return getEncounterUuidFromReferenceUrl(referenceUrl);
    }
}


