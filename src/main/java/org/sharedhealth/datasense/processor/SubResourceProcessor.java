package org.sharedhealth.datasense.processor;

import ca.uhn.fhir.model.api.IResource;
import org.sharedhealth.datasense.handler.FhirResourceHandler;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("subResourceProcessor")
public class SubResourceProcessor implements ResourceProcessor {

    private ResourceProcessor nextProcessor;
    @Autowired
    private List<FhirResourceHandler> fhirResourceHandlers;
    private static final Logger logger = LoggerFactory.getLogger(SubResourceProcessor.class);

    @Override
    public void process(EncounterComposition composition) {
        logger.info("Processing sub resources of encounters for patient:" + composition.getPatientReference().getHealthId());
        deleteExistingEncounter(composition);
        for (IResource resource : composition.getTopLevelResources()) {
            for (FhirResourceHandler fhirResourceHandler : this.fhirResourceHandlers) {
                if (fhirResourceHandler.canHandle(resource)) {
                    logger.debug("Invoking next sub resource handler:" + fhirResourceHandler.getClass().getName());
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
