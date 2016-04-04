package org.sharedhealth.datasense.handler;


import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.IResource;
import org.sharedhealth.datasense.model.DiagnosticOrder;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.DiagnosticOrderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DiagnosticOrderResourceHandler implements FhirResourceHandler {
    @Autowired
    DiagnosticOrderDao diagnosticOrderDao;

    private String extension = "https://sharedhealth.atlassian.net/wiki/display/docs/fhir-extensions#DiagnositicOrderCategory";

    @Override
    public boolean canHandle(IResource resource) {
        return resource instanceof ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder;
    }

    @Override
    public void process(IResource resource, EncounterComposition composition) {
        DiagnosticOrder order = new DiagnosticOrder();
        ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder fhirOrder = (ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder) resource;

        order.setPatientHid(composition.getPatientReference().getHealthId());
        order.setEncounterId(composition.getEncounterReference().getEncounterId());
        order.setOrderStatus(fhirOrder.getItemFirstRep().getStatus());
        setCategory(order, fhirOrder);
//        order.setOrderCode();
        order.setOrderer(fhirOrder.getOrderer().getReference().getValue());
        diagnosticOrderDao.save(order);

    }

    private void setCategory(DiagnosticOrder order, ca.uhn.fhir.model.dstu2.resource.DiagnosticOrder fhirOrder) {
        List<ExtensionDt> undeclaredExtensionsByUrl = fhirOrder.getUndeclaredExtensionsByUrl(extension);
        String category = undeclaredExtensionsByUrl.get(0).getValue().toString();
        order.setOrderCategory(category);
    }

    @Override
    public void deleteExisting(EncounterComposition composition) {

    }
}
