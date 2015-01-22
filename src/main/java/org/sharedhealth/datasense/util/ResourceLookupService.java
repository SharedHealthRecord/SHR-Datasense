package org.sharedhealth.datasense.util;

import org.hl7.fhir.instance.model.ResourceReference;
import org.sharedhealth.datasense.model.fhir.DatasenseResourceReference;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;

public class ResourceLookupService {

    public static DatasenseResourceReference getDatasenseResourceReference(ResourceReference resourceReference, EncounterComposition encounterComposition) {
        for (DatasenseResourceReference datasenseResourceReference : encounterComposition.getResources()) {
            if (datasenseResourceReference.getResourceReference().getReferenceSimple().equals(resourceReference.getReferenceSimple())) {
                return datasenseResourceReference;
            }
        }
        return null;
    }
}
