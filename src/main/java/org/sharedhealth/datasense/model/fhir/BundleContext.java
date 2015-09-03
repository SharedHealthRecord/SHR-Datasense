package org.sharedhealth.datasense.model.fhir;


import ca.uhn.fhir.model.api.IElement;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Composition;

import java.util.ArrayList;
import java.util.List;

public class BundleContext {
    private Bundle bundle;
    private String shrEncounterId;
    private ArrayList<EncounterComposition> encounterCompositions;

    public BundleContext(Bundle bundle, String shrEncounterId) {
        this.bundle = bundle;
        this.shrEncounterId = shrEncounterId;
    }

    public <T extends IElement> List<T> getResourcesOfType(Class<T> type) {
        ArrayList<T> resources = new ArrayList<>();
        List<T> list = bundle.getAllPopulatedChildElementsOfType(type);
        return list;
    }

    public List<EncounterComposition> getEncounterCompositions() {
        if (encounterCompositions == null) {
            List<Composition> compositions = getResourcesOfType(Composition.class);
            encounterCompositions = new ArrayList<>();
            //TODO process only compositions of type encounter
            for (Composition composition : compositions) {
                encounterCompositions.add(new EncounterComposition(composition, this));
            }
        }
        return encounterCompositions;
    }

    public String getShrEncounterId() {
        return shrEncounterId;
    }


    public IResource getResourceForReference(ResourceReferenceDt resourceRef) {
        for (Bundle.Entry entry : bundle.getEntry()) {
            if (entry.getResource().getId().getValue().equals(resourceRef.getReference().getValue())) {
                return entry.getResource();
            }
        }
        return null;
    }
}
