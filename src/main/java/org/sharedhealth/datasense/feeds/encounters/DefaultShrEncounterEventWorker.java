package org.sharedhealth.datasense.feeds.encounters;

import org.sharedhealth.datasense.model.EncounterBundle;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.model.fhir.FHIRBundle;
import org.sharedhealth.datasense.processors.ResourceProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class DefaultShrEncounterEventWorker implements EncounterEventWorker {

    @Autowired
    @Qualifier("patientProcessor")
    ResourceProcessor firstProcessor;

    @Override
    public void process(EncounterBundle encounterBundle) {
        System.out.println("in Default Encounter Worker Processor");
        FHIRBundle fhirBundle = new FHIRBundle(encounterBundle.getResourceOrFeed().getFeed());
        for (EncounterComposition encounterComposition : fhirBundle.getEncounterCompositions()) {
            firstProcessor.process(encounterComposition);
        }
    }
}
