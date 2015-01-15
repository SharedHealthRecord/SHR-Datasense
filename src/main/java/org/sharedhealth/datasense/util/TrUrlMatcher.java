package org.sharedhealth.datasense.util;

public class TrUrlMatcher {

    private final static String CONCEPT_URL_PATH = "/tr/concepts/";
    private final static String REFERENCE_TERM_URL_PATH = "/tr/referenceterms/";
    private final static String MEDICATION_URL_PATH = "/tr/drugs/";

    public static boolean isReferenceTermUrl(String url) {
        return url.contains(REFERENCE_TERM_URL_PATH);
    }

    public static boolean isConceptUrl(String url) {
        return url.contains(CONCEPT_URL_PATH);
    }

    public static boolean isTrMedicationUrl(String system) {
        return system.contains(MEDICATION_URL_PATH);
    }
}
