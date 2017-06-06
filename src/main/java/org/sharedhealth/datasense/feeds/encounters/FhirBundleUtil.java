package org.sharedhealth.datasense.feeds.encounters;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.dstu3.model.Bundle;
import org.springframework.stereotype.Component;


@Component
public class FhirBundleUtil {

    private FhirContext fhirContext = FhirContext.forDstu3();

    public Bundle parseBundle(String content, String type) {
        if (type.equals("xml")) {
            return (Bundle) fhirContext.newXmlParser().parseResource(content);
        } else {
            return (Bundle) fhirContext.newJsonParser().parseResource(content);
        }
    }

    public String encodeBundle(Bundle bundle, String type) {
        if (type.equals("xml")) {
            return fhirContext.newXmlParser().encodeResourceToString(bundle);
        } else {
            return fhirContext.newJsonParser().encodeResourceToString(bundle);
        }
    }

    public FhirContext getFhirContext() {
        return fhirContext;
    }
}
