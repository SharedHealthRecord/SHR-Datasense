package org.sharedhealth.datasense.model.fhir;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.ResourceReference;

import java.util.ArrayList;
import java.util.List;

public class ProviderReference {
    private List<ResourceReference> references;

    public ProviderReference() {
        this.references = new ArrayList<>();
    }

    public List<ResourceReference> getReferences() {
        return references;
    }

    public void addReference(ResourceReference individual) {
        references.add(individual);
    }

    public String getProviderId(ResourceReference reference) {
        return parseUrl(reference.getReferenceSimple());
    }

    public static String parseUrl(String referenceUrl) {
        String s = StringUtils.substringAfterLast(referenceUrl, "/");
        return StringUtils.substringBefore(s, ".json");
    }
}
