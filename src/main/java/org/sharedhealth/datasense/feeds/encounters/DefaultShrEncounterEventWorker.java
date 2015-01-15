package org.sharedhealth.datasense.feeds.encounters;

import org.hl7.fhir.instance.model.AtomFeed;
import org.sharedhealth.datasense.model.EncounterBundle;
import org.sharedhealth.datasense.model.fhir.BundleContext;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.processor.ResourceProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultShrEncounterEventWorker implements EncounterEventWorker {

    @Autowired
    @Qualifier("patientProcessor")
    ResourceProcessor firstProcessor;

    @Override
    public void process(EncounterBundle encounterBundle) {
        AtomFeed feed = encounterBundle.getResourceOrFeed().getFeed();
        BundleContext context = new BundleContext(feed, encounterBundle.getEncounterId());
        List<EncounterComposition> encounterCompositions = context.getEncounterCompositions();
        for (EncounterComposition encounterComposition : encounterCompositions) {
            firstProcessor.process(encounterComposition);
        }
    }


}
