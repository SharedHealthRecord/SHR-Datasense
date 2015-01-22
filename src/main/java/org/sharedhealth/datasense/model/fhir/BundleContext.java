package org.sharedhealth.datasense.model.fhir;

import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Composition;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.ArrayList;
import java.util.List;

public class BundleContext {
    private AtomFeed feed;
    private String shrEncounterId;
    private ArrayList<EncounterComposition> encounterCompositions;

    public BundleContext(AtomFeed feed, String shrEncounterId) {
        this.feed = feed;
        this.shrEncounterId = shrEncounterId;
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

    public Resource getResourceByReferenceFromFeed(ResourceReference resourceReference) {
        for (AtomEntry<? extends Resource> entry : feed.getEntryList()) {
            if (entry.getId().equals(resourceReference.getReferenceSimple())) {
                return entry.getResource();
            }
        }
        return null;
    }

    public List<EncounterComposition> getEncounterCompositions() {
        if (encounterCompositions == null) {
            List<Resource> compositions = getResourcesOfType(ResourceType.Composition);
            encounterCompositions = new ArrayList<>();
            //TODO process only compositions of type encounter
            for (Resource composition : compositions) {
                encounterCompositions.add(new EncounterComposition( (Composition) composition, this));
            }
        }
        return encounterCompositions;
    }

    public String getShrEncounterId() {
        return shrEncounterId;
    }
}
