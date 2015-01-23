package org.sharedhealth.datasense.handler;

import org.hl7.fhir.instance.model.Resource;
import org.sharedhealth.datasense.model.fhir.DatasenseResourceReference;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;

public interface FhirResourceHandler {
    boolean canHandle(Resource resource);

    void process(DatasenseResourceReference datasenseResourceReference, EncounterComposition composition);
}
