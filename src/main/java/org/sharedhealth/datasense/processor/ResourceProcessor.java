package org.sharedhealth.datasense.processor;

import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface ResourceProcessor {
    @Transactional(propagation = Propagation.REQUIRED)
    void process(EncounterComposition composition);

    void setNext(ResourceProcessor nextProcessor);
}
