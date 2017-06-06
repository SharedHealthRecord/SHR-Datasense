package org.sharedhealth.datasense.model.fhir;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Reference;

import java.util.ArrayList;
import java.util.List;

public class ProviderReference {
    private List<Reference> references;

    public ProviderReference() {
        this.references = new ArrayList<>();
    }

    public List<Reference> getReferences() {
        return references;
    }

    public void addReference(Reference individual) {
        references.add(individual);
    }

    public String getProviderId(Reference resourceRef) {
        return parseUrl(resourceRef.getReference());
    }

    public static String parseUrl(String referenceUrl) {
        String s = StringUtils.substringAfterLast(referenceUrl, "/");
        return StringUtils.substringBefore(s, ".json");
    }
}
