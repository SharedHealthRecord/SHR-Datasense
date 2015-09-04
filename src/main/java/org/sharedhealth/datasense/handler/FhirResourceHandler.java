package org.sharedhealth.datasense.handler;

import ca.uhn.fhir.model.api.IResource;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;

public interface FhirResourceHandler {
    boolean canHandle(IResource resource);
    void process(IResource resource, EncounterComposition composition);
    void deleteExisting(EncounterComposition composition);
}
