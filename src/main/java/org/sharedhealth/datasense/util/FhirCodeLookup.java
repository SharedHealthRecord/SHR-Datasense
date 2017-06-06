package org.sharedhealth.datasense.util;


import org.hl7.fhir.dstu3.model.Coding;

import java.util.List;

import static org.sharedhealth.datasense.util.TrUrl.isConceptUrl;
import static org.sharedhealth.datasense.util.TrUrl.isReferenceTermUrl;

public class FhirCodeLookup {

    public static String getReferenceCode(List<Coding> codings) {
        for (Coding coding : codings) {
            if (coding.getSystem() != null && isReferenceTermUrl(coding.getSystem()))
                return coding.getCode();
        }
        return null;
    }

    public static String getConceptId(List<Coding> codings) {
        for (Coding coding : codings) {
            if (coding.getSystem() != null && isConceptUrl(coding.getSystem()))
                return coding.getCode();
        }
        return null;
    }
}
