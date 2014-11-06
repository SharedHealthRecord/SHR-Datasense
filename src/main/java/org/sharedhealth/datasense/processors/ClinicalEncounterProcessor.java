package org.sharedhealth.datasense.processors;

import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.springframework.stereotype.Component;

@Component("clinicalEncounterProcessor")
public class ClinicalEncounterProcessor implements ResourceProcessor {


    private ResourceProcessor nextProcessor;

    @Override
    public void process(EncounterComposition composition) {
        System.out.println("in Clinical Encounter Processor");
        if (nextProcessor != null) {
            nextProcessor.process(composition);
        }
    }

    @Override
    public void setNext(ResourceProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }
}
