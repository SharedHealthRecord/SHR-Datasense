package org.sharedhealth.datasense.handler;

import org.hl7.fhir.instance.model.Resource;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;

public interface FhirResourceHandler {
    boolean canHandle(Resource resource);
    void process(Resource resource, EncounterComposition composition);
    void deleteExisting(EncounterComposition composition);
}
