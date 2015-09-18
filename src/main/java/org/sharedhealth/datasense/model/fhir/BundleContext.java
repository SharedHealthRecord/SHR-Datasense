package org.sharedhealth.datasense.model.fhir;


import ca.uhn.fhir.model.api.IElement;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Composition;
import ca.uhn.fhir.model.primitive.IdDt;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class BundleContext {
    private Bundle bundle;
    private String shrEncounterId;
    private ArrayList<EncounterComposition> encounterCompositions;

    private Logger logger = Logger.getLogger(BundleContext.class);

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
        IdDt resourceReference = resourceRef.getReference();
        for (Bundle.Entry entry : bundle.getEntry()) {
            IResource entryResource = entry.getResource();
            IdDt entryResourceId = entryResource.getId();
            boolean hasFullUrlDefined = !StringUtils.isBlank(entry.getFullUrl());

            if (resourceReference.hasResourceType() && entryResourceId.hasResourceType()
                    && entryResourceId.getValue().equals(resourceReference.getValue()) ) {
                return entryResource;
            } else if (entryResourceId.getIdPart().equals(resourceReference.getIdPart())) {
                return entryResource;
            } else if (hasFullUrlDefined) {
                if (entry.getFullUrl().endsWith(resourceReference.getIdPart())) {
                    return entryResource;
                }
            }
        }
        logger.warn("Could not determine resource for reference:" + resourceReference);
        return null;
    }
}
