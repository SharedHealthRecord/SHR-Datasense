package org.sharedhealth.datasense.processor;

import org.sharedhealth.datasense.model.fhir.EncounterComposition;

public interface ResourceProcessor {
    void process(EncounterComposition composition);
    void setNext(ResourceProcessor nextProcessor);
}
