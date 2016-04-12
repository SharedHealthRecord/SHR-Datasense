package org.sharedhealth.datasense.handler;


import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticReport;
import org.sharedhealth.datasense.model.Observation;
import org.sharedhealth.datasense.model.fhir.BundleContext;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.model.fhir.ProviderReference;
import org.sharedhealth.datasense.repository.DiagnosticReportDao;
import org.sharedhealth.datasense.util.TrUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.sharedhealth.datasense.util.TrUrl.isConceptUrl;

@Component
public class DiagnosticReportResourceHandler implements FhirResourceHandler {
    @Autowired
    DiagnosticReportDao diagnosticReportDao;
    @Autowired
    ObservationResourceHandler observationResourceHandler;

    private BundleContext bundleContext;

    @Override
    public boolean canHandle(IResource resource) {
        return resource instanceof DiagnosticReport;
    }

    @Override
    public void process(IResource resource, EncounterComposition composition) {

        DiagnosticReport fhirDiagnosticReport = (DiagnosticReport) resource;

        org.sharedhealth.datasense.model.DiagnosticReport diagnosticReport = new org.sharedhealth.datasense.model.DiagnosticReport();
        diagnosticReport.setPatientHid(composition.getPatientReference().getHealthId());
        diagnosticReport.setEncounterId(composition.getEncounterReference().getEncounterId());
        diagnosticReport.setReportDate(fhirDiagnosticReport.getIssued());
        diagnosticReport.setFulfiller(ProviderReference.parseUrl(fhirDiagnosticReport.getPerformer().getReference().getValue()));
        setCategory(fhirDiagnosticReport, diagnosticReport);
        populateOrderCodeAndConcept(fhirDiagnosticReport.getCode().getCoding(), diagnosticReport);
        int reportId = diagnosticReportDao.save(diagnosticReport);
        saveResultObservations(composition, fhirDiagnosticReport, reportId);

    }

    @Override
    public void deleteExisting(EncounterComposition composition) {
        diagnosticReportDao.deleteExisting(composition.getPatientReference().getHealthId(), composition.getEncounterReference().getEncounterId());
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

    private void populateOrderCodeAndConcept(List<CodingDt> coding, org.sharedhealth.datasense.model.DiagnosticReport diagnosticOrder) {
        for (CodingDt codingDt : coding) {
            if (isConceptUrl(codingDt.getSystem())) {
                diagnosticOrder.setReportConcept(codingDt.getCode());
            } else if (TrUrl.isReferenceTermUrl(codingDt.getSystem())) {
                diagnosticOrder.setReportCode(codingDt.getCode());
            }
        }
    }

}
