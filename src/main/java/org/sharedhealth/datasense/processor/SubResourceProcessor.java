package org.sharedhealth.datasense.processor;

import org.sharedhealth.datasense.handler.FhirResourceHandler;
import org.sharedhealth.datasense.model.fhir.DatasenseResourceReference;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("subResourceProcessor")
public class SubResourceProcessor implements ResourceProcessor {

    private ResourceProcessor nextProcessor;
    @Autowired
    private List<FhirResourceHandler> fhirResourceHandlers;

    @Override
    public void process(EncounterComposition composition) {
        for (DatasenseResourceReference resource : composition.getResources()) {
            if (resource.getValue() != null) continue;
            for (FhirResourceHandler fhirResourceHandler : fhirResourceHandlers) {
                if (fhirResourceHandler.canHandle(resource.getResourceValue())) {
                    fhirResourceHandler.process(resource, composition);
                }
            }
        }
    }

    @Override
    public void setNext(ResourceProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }
}
