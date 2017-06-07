package org.sharedhealth.datasense.model.fhir;


import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;

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

    public List<EncounterComposition> getEncounterCompositions() {
        if (encounterCompositions == null) {
            List<Composition> compositions = getCompositions();
            encounterCompositions = new ArrayList<>();
            //TODO process only compositions of type encounter
            for (Composition composition : compositions) {
                encounterCompositions.add(new EncounterComposition(composition, this));
            }
        }
        return encounterCompositions;
    }

    //todo: verify
    private List<Composition> getCompositions() {
        List<Composition> compositions = new ArrayList<>();
        List<Bundle.BundleEntryComponent> entry = bundle.getEntry();
        for (Bundle.BundleEntryComponent bundleEntryComponent : entry) {
            if (bundleEntryComponent.getResource().getResourceType().name().equals(new Composition().getResourceType().name())) {
                compositions.add((Composition) bundleEntryComponent.getResource());
            }
        }

        return compositions;
    }

    public String getShrEncounterId() {
        return shrEncounterId;
    }


    public Resource getResourceForReference(Reference resourceRef) {
        String resourceReferenceIdPart = StringUtils.substringAfter(resourceRef.getReference(), "urn:uuid:");
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource entryResource = entry.getResource();
            String entryResourceId = entryResource.getId();
            boolean hasFullUrlDefined = !StringUtils.isBlank(entry.getFullUrl());

            if (entryResourceId.endsWith(resourceReferenceIdPart)) {
                return entryResource;
            } else if (hasFullUrlDefined) {
                if (entry.getFullUrl().endsWith(resourceReferenceIdPart)) {
                    return entryResource;
                }
            }
        }
        logger.warn("Could not determine resource for reference:" + resourceReferenceIdPart);
        return null;
    }
}
