package org.sharedhealth.datasense.handler;


import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.Resource;
import org.sharedhealth.datasense.model.DiagnosticOrder;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.model.fhir.ProviderReference;
import org.sharedhealth.datasense.repository.DiagnosticOrderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.util.List;

import static org.sharedhealth.datasense.util.TrUrl.isConceptUrl;
import static org.sharedhealth.datasense.util.TrUrl.isReferenceTermUrl;

@Component
public class ProcedureRequestResourceHandler implements FhirResourceHandler {
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
        ProcedureRequest fhirProcedureRequest = (ProcedureRequest) resource;
        createAndSaveDiagnosticOrder(composition, fhirProcedureRequest);
    }

    private void createAndSaveDiagnosticOrder(EncounterComposition composition, ProcedureRequest fhirDiagnosticOrder) {
        DiagnosticOrder diagnosticOrder = new DiagnosticOrder();
        diagnosticOrder.setPatientHid(composition.getPatientReference().getHealthId());
        diagnosticOrder.setEncounterId(composition.getEncounterReference().getEncounterId());
        diagnosticOrder.setOrderStatus(fhirDiagnosticOrder.getStatus().toCode());
        diagnosticOrder.setOrderDate(fhirDiagnosticOrder.getAuthoredOn());

        String ordererId = ProviderReference.parseUrl(fhirDiagnosticOrder.getRequester().getAgent().getReference());
        diagnosticOrder.setOrderer(ordererId);

        setCategory(diagnosticOrder, fhirDiagnosticOrder);
        populateOrderCodeAndConcept(diagnosticOrder, fhirDiagnosticOrder);
        if (diagnosticOrder.getOrderConcept() == null && diagnosticOrder.getCode() == null) return;
        setConcatenatedShrOrderUuid(fhirDiagnosticOrder, diagnosticOrder);

        diagnosticOrderDao.save(diagnosticOrder);
    }

    private void setConcatenatedShrOrderUuid(ProcedureRequest fhirDiagnosticOrder, DiagnosticOrder diagnosticOrder) {
        diagnosticOrder.setShrOrderUuid(diagnosticOrder.getEncounterId() + ":" + StringUtils.substringAfter(fhirDiagnosticOrder.getId(), "urn:uuid:"));
    }

    private void setCategory(DiagnosticOrder order, ProcedureRequest fhirOrder) {
        CodeableConcept categoryFirstRep = fhirOrder.getCategoryFirstRep();
        if (null != categoryFirstRep) {
            Coding coding = categoryFirstRep.getCodingFirstRep();
            if (null != coding)
                order.setOrderCategory(coding.getCode());
        }
        if (order.getOrderCategory() == null) throw new RuntimeException("Category is not present");
    }

    private void populateOrderCodeAndConcept(DiagnosticOrder diagnosticOrder, ProcedureRequest fhirDiagnosticOrder) {
        CodeableConcept code = fhirDiagnosticOrder.getCode();
        List<Coding> codings = code.getCoding();
        for (Coding coding : codings) {
            if (isConceptUrl(coding.getSystem())) {
                diagnosticOrder.setOrderConcept(coding.getCode());
            } else if (isReferenceTermUrl(coding.getSystem())) {
                diagnosticOrder.setcode(coding.getCode());
            }
        }
    }


    @Override
    public void deleteExisting(EncounterComposition composition) {
        diagnosticOrderDao.deleteExisting(composition.getEncounterReference().getEncounterId());
    }
}
