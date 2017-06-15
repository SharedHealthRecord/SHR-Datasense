package org.sharedhealth.datasense.handler;


import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Resource;
import org.sharedhealth.datasense.model.ProcedureRequest;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.model.fhir.ProviderReference;
import org.sharedhealth.datasense.repository.ProcedureRequestDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.util.List;

import static org.sharedhealth.datasense.util.TrUrl.isConceptUrl;
import static org.sharedhealth.datasense.util.TrUrl.isReferenceTermUrl;

@Component
public class ProcedureRequestResourceHandler implements FhirResourceHandler {
    @Autowired
    ProcedureRequestDao procedureRequestDao;

    @Override
    public boolean canHandle(Resource resource) {
        return resource instanceof org.hl7.fhir.dstu3.model.ProcedureRequest;
    }

    @Override
    public void process(Resource resource, EncounterComposition composition) {
        org.hl7.fhir.dstu3.model.ProcedureRequest fhirProcedureRequest = (org.hl7.fhir.dstu3.model.ProcedureRequest) resource;
        createAndSaveDiagnosticOrder(composition, fhirProcedureRequest);
    }

    private void createAndSaveDiagnosticOrder(EncounterComposition composition, org.hl7.fhir.dstu3.model.ProcedureRequest fhirDiagnosticOrder) {
        ProcedureRequest procedureRequest = new ProcedureRequest();
        procedureRequest.setPatientHid(composition.getPatientReference().getHealthId());
        procedureRequest.setEncounterId(composition.getEncounterReference().getEncounterId());
        procedureRequest.setOrderStatus(fhirDiagnosticOrder.getStatus().toCode());
        procedureRequest.setOrderDate(fhirDiagnosticOrder.getAuthoredOn());

        String ordererId = ProviderReference.parseUrl(fhirDiagnosticOrder.getRequester().getAgent().getReference());
        procedureRequest.setOrderer(ordererId);

        setCategory(procedureRequest, fhirDiagnosticOrder);
        populateOrderCodeAndConcept(procedureRequest, fhirDiagnosticOrder);
        if (procedureRequest.getOrderConcept() == null && procedureRequest.getCode() == null) return;
        setConcatenatedShrOrderUuid(fhirDiagnosticOrder, procedureRequest);

        this.procedureRequestDao.save(procedureRequest);
    }

    private void setConcatenatedShrOrderUuid(org.hl7.fhir.dstu3.model.ProcedureRequest fhirDiagnosticOrder, ProcedureRequest procedureRequest) {
        procedureRequest.setShrOrderUuid(procedureRequest.getEncounterId() + ":" + StringUtils.substringAfter(fhirDiagnosticOrder.getId(), "urn:uuid:"));
    }

    private void setCategory(ProcedureRequest order, org.hl7.fhir.dstu3.model.ProcedureRequest fhirOrder) {
        CodeableConcept categoryFirstRep = fhirOrder.getCategoryFirstRep();
        if (null != categoryFirstRep) {
            Coding coding = categoryFirstRep.getCodingFirstRep();
            if (null != coding)
                order.setOrderCategory(coding.getCode());
        }
        if (order.getOrderCategory() == null) throw new RuntimeException("Category is not present");
    }

    private void populateOrderCodeAndConcept(ProcedureRequest procedureRequest, org.hl7.fhir.dstu3.model.ProcedureRequest fhirDiagnosticOrder) {
        CodeableConcept code = fhirDiagnosticOrder.getCode();
        List<Coding> codings = code.getCoding();
        for (Coding coding : codings) {
            if (isConceptUrl(coding.getSystem())) {
                procedureRequest.setOrderConcept(coding.getCode());
            } else if (isReferenceTermUrl(coding.getSystem())) {
                procedureRequest.setcode(coding.getCode());
            }
        }
    }


    @Override
    public void deleteExisting(EncounterComposition composition) {
        procedureRequestDao.deleteExisting(composition.getEncounterReference().getEncounterId());
    }
}
