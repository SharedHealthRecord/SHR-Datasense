package org.sharedhealth.datasense.util;

public class TrUrl {

    private final static String CONCEPT_URL_PATH = "/tr/concepts/";
    private final static String REFERENCE_TERM_URL_PATH = "/tr/referenceterms/";
    private final static String MEDICATION_URL_PATH = "/tr/drugs/";

    public static boolean isReferenceTermUrl(String url) {
        if (url == null) {
            return false;
        }
        return url.contains(REFERENCE_TERM_URL_PATH);
    }

    public static boolean isConceptUrl(String url) {
        if (url == null) {
            return false;
        }
        return url.contains(CONCEPT_URL_PATH);
    }

    public static boolean isTrMedicationUrl(String system) {
        if (system == null) {
            return false;
        }
        return system.contains(MEDICATION_URL_PATH);
    }
}
