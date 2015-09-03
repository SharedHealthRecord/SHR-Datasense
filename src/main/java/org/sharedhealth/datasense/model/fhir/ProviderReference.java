package org.sharedhealth.datasense.model.fhir;

import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProviderReference {
    private List<ResourceReferenceDt> references;

    public ProviderReference() {
        this.references = new ArrayList<>();
    }

    public List<ResourceReferenceDt> getReferences() {
        return references;
    }

    public void addReference(ResourceReferenceDt individual) {
        references.add(individual);
    }

    public String getProviderId(ResourceReferenceDt resourceRef) {
        return parseUrl(resourceRef.getReference().getValue());
    }

    public static String parseUrl(String referenceUrl) {
        String s = StringUtils.substringAfterLast(referenceUrl, "/");
        return StringUtils.substringBefore(s, ".json");
    }
}
