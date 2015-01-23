package org.sharedhealth.datasense.util;

import org.hl7.fhir.instance.model.Coding;

import java.util.List;

import static org.sharedhealth.datasense.util.TrUrl.isConceptUrl;
import static org.sharedhealth.datasense.util.TrUrl.isReferenceTermUrl;

public class FhirCodeLookup {

    public static String getReferenceCode(List<Coding> codings) {
        for (Coding coding : codings) {
            if (coding.getSystemSimple() != null && isReferenceTermUrl(coding.getSystemSimple()))
                return coding.getCodeSimple();
        }
        return null;
    }

    public static String getConceptId(List<Coding> codings) {
        for (Coding coding : codings) {
            if (coding.getSystemSimple() != null && isConceptUrl(coding.getSystemSimple()))
                return coding.getCodeSimple();
        }
        return null;
    }
}
