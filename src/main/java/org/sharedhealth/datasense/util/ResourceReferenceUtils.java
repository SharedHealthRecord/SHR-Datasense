package org.sharedhealth.datasense.util;

import org.hl7.fhir.dstu3.model.Reference;

public class ResourceReferenceUtils {

    public static String getOrderUuidFromResourceReference(Reference resourceReferenceDt) {
        String referenceUrl = getReferenceUrlFromResourceReference(resourceReferenceDt);
        return getOrderUuidFromReferenceUrl(referenceUrl);
    }

    public static String getOrderUuidFromReferenceUrl(String referenceUrl) {
        if (referenceUrl.isEmpty()) return null;
        return referenceUrl.substring(referenceUrl.lastIndexOf('/') + 1);
    }

    public static String getEncounterUuidFromReferenceUrl(String referenceUrl) {
        if (referenceUrl.isEmpty()) return null;
        if (referenceUrl.contains("#"))
            referenceUrl = referenceUrl.substring(0, referenceUrl.lastIndexOf('#'));
        return referenceUrl.substring(referenceUrl.lastIndexOf('/') + 1);
    }

    public static String getReferenceUrlFromResourceReference(Reference resourceReferenceDt) {
        return resourceReferenceDt.getReference();
    }
}
