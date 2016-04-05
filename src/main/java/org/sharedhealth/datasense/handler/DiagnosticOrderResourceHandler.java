package org.sharedhealth.datasense.handler;


import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import org.sharedhealth.datasense.model.DiagnosticOrder;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.model.fhir.ProviderReference;
import org.sharedhealth.datasense.repository.DiagnosticOrderDao;
import org.sharedhealth.datasense.util.TrUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static org.sharedhealth.datasense.util.TrUrl.isConceptUrl;

@Component
public class DiagnosticOrderResourceHandler implements FhirResourceHandler {
    @Autowired
    DiagnosticOrderDao diagnosticOrderDao;

    private static final String FHIR_DIAGNOSIC_ORDER_CATEGORY_EXTENSION_URL = "https://sharedhealth.atlassian.net/wiki/display/docs/fhir-extensions#DiagnositicOrderCategory";

    @Override
    public boolean canHandle(IResource resource) {
        return resource instanceof ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder;
    }

    @Override
    public void process(IResource resource, EncounterComposition composition) {
        ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder fhirDiagnosticOrder = (ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder) resource;

        for (ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder.Item item : fhirDiagnosticOrder.getItem()) {
            createAndSaveDiagnosticOrder(composition, fhirDiagnosticOrder, item);
        }
    }

    private void createAndSaveDiagnosticOrder(EncounterComposition composition, ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder fhirDiagnosticOrder, ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder.Item item) {
        DiagnosticOrder diagnosticOrder = new DiagnosticOrder();
        diagnosticOrder.setPatientHid(composition.getPatientReference().getHealthId());
        diagnosticOrder.setEncounterId(composition.getEncounterReference().getEncounterId());
        diagnosticOrder.setOrderStatus(item.getStatus());
        setCategory(diagnosticOrder, fhirDiagnosticOrder);
        populateOrderCodeAndConcept(item.getCode().getCoding(), diagnosticOrder);
        if(diagnosticOrder.getOrderCode() == null && diagnosticOrder.getOrderConcept() == null) return;
        String ordererId = ProviderReference.parseUrl(fhirDiagnosticOrder.getOrderer().getReference().getValue());
        diagnosticOrder.setOrderer(ordererId);
        diagnosticOrderDao.save(diagnosticOrder);
    }

    private void populateOrderCodeAndConcept(List<CodingDt> coding, DiagnosticOrder diagnosticOrder) {
        for (CodingDt codingDt : coding) {
            if (isConceptUrl(codingDt.getSystem())) {
                diagnosticOrder.setOrderConcept(codingDt.getCode());
            } else if (TrUrl.isReferenceTermUrl(codingDt.getSystem())) {
                diagnosticOrder.setOrderCode(codingDt.getCode());
            }
        }
    }

    private void setCategory(DiagnosticOrder order, ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder fhirOrder) {
        List<ExtensionDt> undeclaredExtensionsByUrl = fhirOrder.getUndeclaredExtensionsByUrl(FHIR_DIAGNOSIC_ORDER_CATEGORY_EXTENSION_URL);
        if (CollectionUtils.isEmpty(undeclaredExtensionsByUrl)) return;
        String category = undeclaredExtensionsByUrl.get(0).getValue().toString();
        order.setOrderCategory(category);
    }

    @Override
    public void deleteExisting(EncounterComposition composition) {
        diagnosticOrderDao.deleteExisting(composition.getPatientReference().getHealthId(), composition.getEncounterReference().getEncounterId());
    }
}
