package org.sharedhealth.datasense.processors;

import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("serviceProviderProcessor")
public class ServiceProviderProcessor implements ResourceProcessor {

    @Autowired
    @Qualifier("clinicalEncounterProcessor")
    private ResourceProcessor nextProcessor;

    @Override
    public void process(EncounterComposition composition) {
        System.out.println("in Service Provider Processor");
        if(nextProcessor != null) {
            nextProcessor.process(composition);
        }
    }

    @Override
    public void setNext(ResourceProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }
}
