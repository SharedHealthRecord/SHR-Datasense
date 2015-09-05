package org.sharedhealth.datasense.processor;

import ca.uhn.fhir.model.api.IResource;
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
        deleteExistingEncounter(composition);
        for (IResource resource : composition.getTopLevelResources()) {
            for (FhirResourceHandler fhirResourceHandler : this.fhirResourceHandlers) {
                if (fhirResourceHandler.canHandle(resource)) {
                    fhirResourceHandler.process(resource, composition);
                }
            }
        }
    }

    private void deleteExistingEncounter(EncounterComposition composition) {
        for (FhirResourceHandler fhirResourceHandler : this.fhirResourceHandlers) {
            fhirResourceHandler.deleteExisting(composition);
        }
    }

    @Override
    public void setNext(ResourceProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }
}
