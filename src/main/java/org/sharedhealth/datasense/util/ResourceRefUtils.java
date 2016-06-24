package org.sharedhealth.datasense.util;

import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;

public class ResourceRefUtils {

    public static String getOrderUuidFromResourceReference(ResourceReferenceDt resourceReferenceDt) {
        String referenceUrl = resourceReferenceDt.getReference().getValue();
        if(referenceUrl.isEmpty()) return null;
        return referenceUrl.substring(referenceUrl.lastIndexOf('/') + 1);
    }
}
