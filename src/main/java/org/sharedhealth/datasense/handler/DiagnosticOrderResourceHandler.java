package org.sharedhealth.datasense.handler;


import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.Resource;
import org.sharedhealth.datasense.model.DiagnosticOrder;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.model.fhir.ProviderReference;
import org.sharedhealth.datasense.repository.DiagnosticOrderDao;
import org.sharedhealth.datasense.util.TrUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.sharedhealth.datasense.util.TrUrl.isConceptUrl;

@Component
public class DiagnosticOrderResourceHandler implements FhirResourceHandler {
    @Autowired
    DiagnosticOrderDao diagnosticOrderDao;

    private static final String FHIR_DIAGNOSTIC_ORDER_CATEGORY_EXTENSION_URL = "https://sharedhealth.atlassian.net/wiki/display/docs/fhir-extensions#DiagnosticOrderCategory";
    private static final String FHIR_DIAGNOSTIC_ORDER_LAB_CATEGORY_CODE = "LAB";

    @Override
    public boolean canHandle(Resource resource) {
        return resource instanceof ProcedureRequest;
    }

    @Override
    public void process(Resource resource, EncounterComposition composition) {
        ProcedureRequest fhirDiagnosticOrder = (ProcedureRequest) resource;

//        for (ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder.Item item : fhirDiagnosticOrder.getItem()) {
//            createAndSaveDiagnosticOrder(composition, fhirDiagnosticOrder, item);
//        }
    }

//    private void createAndSaveDiagnosticOrder(EncounterComposition composition, ProcedureRequest fhirDiagnosticOrder, ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder.Item item) {
//        DiagnosticOrder diagnosticOrder = new DiagnosticOrder();
//        diagnosticOrder.setPatientHid(composition.getPatientReference().getHealthId());
//        diagnosticOrder.setEncounterId(composition.getEncounterReference().getEncounterId());
//        diagnosticOrder.setOrderStatus(item.getStatus());
//        setConcatenatedShrOrderUuid(fhirDiagnosticOrder, diagnosticOrder);
//        setCategory(diagnosticOrder, fhirDiagnosticOrder);
//        populateOrderCodeAndConcept(item.getCode().getCoding(), diagnosticOrder);
//        if (diagnosticOrder.getCode() == null && diagnosticOrder.getOrderConcept() == null) return;
//        setOrderDate(item, diagnosticOrder);
//        String ordererId = ProviderReference.parseUrl(fhirDiagnosticOrder.getOrderer().getReference().getValue());
//        diagnosticOrder.setOrderer(ordererId);
//        diagnosticOrderDao.save(diagnosticOrder);
//    }

//    private void setConcatenatedShrOrderUuid(ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder fhirDiagnosticOrder, DiagnosticOrder diagnosticOrder) {
//        diagnosticOrder.setShrOrderUuid(diagnosticOrder.getEncounterId() + ":" + fhirDiagnosticOrder.getId().getIdPart());
//    }
//
//    private void setOrderDate(ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder.Item item, DiagnosticOrder diagnosticOrder) {
//        List<ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder.Event> events = item.getEvent();
//        Collections.sort(events, new Comparator<ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder.Event>() {
//            public int compare(ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder.Event o1, ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder.Event o2) {
//                if (o1.getDateTime() == null || o2.getDateTime() == null)
//                    return 0;
//                return o1.getDateTime().compareTo(o2.getDateTime());
//            }
//        });
//        if (!CollectionUtils.isEmpty(events)) {
//            ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder.Event latestEvent = events.get(events.size() - 1);
//            diagnosticOrder.setOrderDate(latestEvent.getDateTime());
//        }
//    }
//
//    private void populateOrderCodeAndConcept(List<CodingDt> coding, DiagnosticOrder diagnosticOrder) {
//        for (CodingDt codingDt : coding) {
//            if (isConceptUrl(codingDt.getSystem())) {
//                diagnosticOrder.setOrderConcept(codingDt.getCode());
//            } else if (TrUrl.isReferenceTermUrl(codingDt.getSystem())) {
//                diagnosticOrder.setcode(codingDt.getCode());
//            }
//        }
//    }
//
//    private void setCategory(DiagnosticOrder order, ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder fhirOrder) {
//        List<ExtensionDt> undeclaredExtensionsByUrl = fhirOrder.getUndeclaredExtensionsByUrl(FHIR_DIAGNOSTIC_ORDER_CATEGORY_EXTENSION_URL);
//        if (CollectionUtils.isEmpty(undeclaredExtensionsByUrl)) {
//            order.setOrderCategory(FHIR_DIAGNOSTIC_ORDER_LAB_CATEGORY_CODE);
//            return;
//        }
//        String category = undeclaredExtensionsByUrl.get(0).getValue().toString();
//        order.setOrderCategory(category);
//    }

    @Override
    public void deleteExisting(EncounterComposition composition) {
        diagnosticOrderDao.deleteExisting(composition.getEncounterReference().getEncounterId());
    }
}
