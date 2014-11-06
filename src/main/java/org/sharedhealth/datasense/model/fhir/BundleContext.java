package org.sharedhealth.datasense.model.fhir;

import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.ArrayList;
import java.util.List;

public class BundleContext {
    private AtomFeed feed;

    public BundleContext(AtomFeed feed) {
        this.feed = feed;
    }

    public List<Resource> getResourcesOfType(ResourceType type) {
        ArrayList<Resource> resources = new ArrayList<>();
        for (AtomEntry<? extends Resource> entry : feed.getEntryList()) {
            if (entry.getResource().getResourceType().equals(type)) {
                resources.add(entry.getResource());
            }
        }
        return resources;
    }

    public Resource getResourceByReference(ResourceReference resourceReference) {
        for (AtomEntry<? extends Resource> entry : feed.getEntryList()) {
            //TODO we need to fix resource reference as par FHIR bundle spec
            if (entry.getId().equals("urn:" +  resourceReference.getReferenceSimple())) {
                return entry.getResource();
            }
        }
        return null;
    }
}
