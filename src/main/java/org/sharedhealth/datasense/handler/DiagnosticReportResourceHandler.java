package org.sharedhealth.datasense.handler;


import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticReport;
import org.sharedhealth.datasense.model.Observation;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.model.fhir.ProviderReference;
import org.sharedhealth.datasense.repository.DiagnosticOrderDao;
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
    DiagnosticOrderDao diagnosticOrderDao;

    @Override
    public boolean canHandle(IResource resource) {
        return resource instanceof DiagnosticReport;
    }

    @Override
    public void process(IResource resource, EncounterComposition composition) {

        DiagnosticReport fhirDiagnosticReport = (DiagnosticReport) resource;

        org.sharedhealth.datasense.model.DiagnosticReport diagnosticReport = new org.sharedhealth.datasense.model.DiagnosticReport();
        String encounterId = composition.getEncounterReference().getEncounterId();

        diagnosticReport.setEncounterId(encounterId);
        diagnosticReport.setPatientHid(composition.getPatientReference().getHealthId());
        diagnosticReport.setReportDate(fhirDiagnosticReport.getIssued());
        diagnosticReport.setFulfiller(ProviderReference.parseUrl(fhirDiagnosticReport.getPerformer().getReference().getValue()));
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
        List<ResourceReferenceDt> resultReferences = fhirDiagnosticReport.getResult();
        for (ResourceReferenceDt resultReference : resultReferences) {
            IResource observationForReference = composition.getResourceByReference(resultReference);
            observationResourceHandler.mapObservation(composition, new Observation(), observationForReference, reportId);
        }
    }

    private void setCategory(DiagnosticReport fhirDiagnosticReport, org.sharedhealth.datasense.model.DiagnosticReport diagnosticReport) {
        if (!fhirDiagnosticReport.getCategory().isEmpty())
            diagnosticReport.setReportCategory(fhirDiagnosticReport.getCategory().getCoding().get(0).getCode());
        else
            diagnosticReport.setReportCategory("LAB");
    }

    private void populateOrderCodeAndConcept(List<CodingDt> coding, org.sharedhealth.datasense.model.DiagnosticReport diagnosticReport) {
        for (CodingDt codingDt : coding) {
            if (isConceptUrl(codingDt.getSystem())) {
                diagnosticReport.setReportConcept(codingDt.getCode());
            } else if (TrUrl.isReferenceTermUrl(codingDt.getSystem())) {
                diagnosticReport.setCode(codingDt.getCode());
            }
        }
    }

    private Integer populateShrOrderId(DiagnosticReport fhirDiagnosticReport) {
        if (hasShrOrderUuid(fhirDiagnosticReport.getRequest()))
            return getConcatenatedShrOrderUuidFromRequest(fhirDiagnosticReport.getRequest());
        return getShrOrderIdFromEncounter(fhirDiagnosticReport);
    }

    private boolean hasShrOrderUuid(List<ResourceReferenceDt> request) {
        if (null == request) return false;
        String referenceUrl = getReferenceUrlFromResourceReference(request.get(0));
        if (referenceUrl.isEmpty()) return false;
        return referenceUrl.contains("#" + new DiagnosticOrder().getResourceName());
    }

    private Integer getConcatenatedShrOrderUuidFromRequest(List<ResourceReferenceDt> request) {
        String referenceUrl = getReferenceUrlFromResourceReference(request.get(0));
        String orderEncounterId = getEncounterUuidFromReferenceUrl(referenceUrl);
        String shrOrderUuid = getOrderUuidFromReferenceUrl(referenceUrl);

        String concatenatedShrOrderUuid = orderEncounterId + ":" + shrOrderUuid;
        return diagnosticOrderDao.getOrderId(concatenatedShrOrderUuid);

    }


    private Integer getShrOrderIdFromEncounter(DiagnosticReport fhirDiagnosticReport) {
        String orderEncounterId = getOrderEncounterId(fhirDiagnosticReport);
        for (CodingDt codingDt : fhirDiagnosticReport.getCode().getCoding()) {
            if (isConceptUrl(codingDt.getSystem())) {
                return diagnosticOrderDao.getOrderId(orderEncounterId, codingDt.getCode());
            }
        }
        return null;
    }

    private String getOrderEncounterId(DiagnosticReport fhirDiagnosticReport) {
        List<ResourceReferenceDt> request = fhirDiagnosticReport.getRequest();
        if (null == request) return null;
        String referenceUrl = getReferenceUrlFromResourceReference(request.get(0));
        return getEncounterUuidFromReferenceUrl(referenceUrl);
    }
}


