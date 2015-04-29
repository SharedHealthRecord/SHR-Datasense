package org.sharedhealth.datasense.processor;

import org.hl7.fhir.instance.model.Resource;
import org.sharedhealth.datasense.handler.FhirResourceHandler;
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
        for (Resource resource : composition.getParentResources()) {
            for (FhirResourceHandler fhirResourceHandler : fhirResourceHandlers) {
                fhirResourceHandler.deleteExisting(composition);
                if (fhirResourceHandler.canHandle(resource)) {
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
