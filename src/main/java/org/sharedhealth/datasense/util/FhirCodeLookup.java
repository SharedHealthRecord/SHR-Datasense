package org.sharedhealth.datasense.util;

import ca.uhn.fhir.model.dstu2.composite.CodingDt;

import java.util.List;

import static org.sharedhealth.datasense.util.TrUrl.isConceptUrl;
import static org.sharedhealth.datasense.util.TrUrl.isReferenceTermUrl;

public class FhirCodeLookup {

    public static String getReferenceCode(List<CodingDt> codings) {
        for (CodingDt coding : codings) {
            if (coding.getSystem() != null && isReferenceTermUrl(coding.getSystem()))
                return coding.getCode();
        }
        return null;
    }

    public static String getConceptId(List<CodingDt> codings) {
        for (CodingDt coding : codings) {
            if (coding.getSystem() != null && isConceptUrl(coding.getSystem()))
                return coding.getCode();
        }
        return null;
    }
}
