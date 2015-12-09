package org.sharedhealth.datasense.feeds.encounters;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import org.sharedhealth.datasense.model.EncounterBundle;
import org.sharedhealth.datasense.model.fhir.BundleContext;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.processor.ResourceProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultShrEncounterEventWorker implements EncounterEventWorker {

    @Autowired
    @Qualifier("patientProcessor")
    ResourceProcessor firstProcessor;
    private static final Logger logger = LoggerFactory.getLogger(DefaultShrEncounterEventWorker.class);

    @Override
    public void process(EncounterBundle encounterBundle) {
        Bundle bundle = encounterBundle.getBundle();
        BundleContext context = new BundleContext(bundle, encounterBundle.getEncounterId());
        List<EncounterComposition> encounterCompositions = context.getEncounterCompositions();
        for (EncounterComposition encounterComposition : encounterCompositions) {
            logger.debug("Invoking processor for encounter bundle:" + encounterBundle.getEncounterId());
            firstProcessor.process(encounterComposition);
        }
        logger.debug("Done processing encounter bundle:" + encounterBundle.getEncounterId());

    }


}
