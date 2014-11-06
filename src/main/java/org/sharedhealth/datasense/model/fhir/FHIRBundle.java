package org.sharedhealth.datasense.model.fhir;

import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Composition;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.ArrayList;
import java.util.List;

public class FHIRBundle {
    private final BundleContext context;
    private List<EncounterComposition> encounterCompositions;


    public FHIRBundle(AtomFeed feed) {
        this.context = new BundleContext(feed);
    }

    public List<EncounterComposition> getEncounterCompositions() {
        if (encounterCompositions == null) {
            List<Resource> compositions = context.getResourcesOfType(ResourceType.Composition);
            encounterCompositions = new ArrayList<EncounterComposition>();
            for (Resource composition : compositions) {
                encounterCompositions.add(new EncounterComposition( (Composition) composition, context));
            }
        }
        return encounterCompositions;
    }
}
