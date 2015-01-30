package org.sharedhealth.datasense.model.fhir;

import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class EncounterComposition {
    private final Composition composition;
    private final BundleContext context;
    private final PatientReference patientReference;
    private ServiceProviderReference serviceProviderReference;
    private EncounterReference encounterReference;

    public EncounterComposition(Composition composition, BundleContext context) {
        this.composition = composition;
        this.context = context;
        encounterReference = new EncounterReference(composition.getEncounter(),
                (Encounter) context.getResourceByReferenceFromFeed(composition.getEncounter()));
        patientReference = new PatientReference(encounterReference.getEncounterReferenceValue().getSubject());
        ResourceReference serviceProvider = encounterReference.getEncounterReferenceValue().getServiceProvider();
        if (serviceProvider != null) {
            serviceProviderReference = new ServiceProviderReference(serviceProvider);
        }
    }

    public ArrayList<Resource> loadResourcesFromComposition() {
        ArrayList<Resource> resources = new ArrayList<>();
        for (Composition.SectionComponent sectionComponent : composition.getSection()) {
            if (!sectionComponent.getContent().getDisplaySimple().equalsIgnoreCase("encounter")) {
                resources.add(context.getResourceByReferenceFromFeed(sectionComponent.getContent()));
            }
        }
        return resources;
    }

    private ArrayList<AtomEntry<? extends Resource>> loadAtomEntriesFromComposition() {
        ArrayList<AtomEntry<? extends Resource>> atomEntries = new ArrayList<>();
        for (Composition.SectionComponent sectionComponent : composition.getSection()) {
            if (!sectionComponent.getContent().getDisplaySimple().equalsIgnoreCase("encounter")) {
                atomEntries.add(context.getAtomEntryFromFeed(sectionComponent.getContent()));
            }
        }
        return atomEntries;
    }

    public ArrayList<Resource> getParentResources() {
        HashSet<String> references = getChildResourceReferences();
        return getParentResources(references);
    }

    private ArrayList<Resource> getParentResources(HashSet<String> references) {
        ArrayList<Resource> parentResources = new ArrayList<>();
        for (AtomEntry<? extends Resource> atomEntry : loadAtomEntriesFromComposition()) {
            if(!references.contains(atomEntry.getId())) {
                parentResources.add(atomEntry.getResource());
            }
        }
        return parentResources;
    }

    private HashSet<String> getChildResourceReferences() {
        HashSet<String> references = new HashSet<>();
        for (Resource resource : loadResourcesFromComposition()) {
            List<Property> children = resource.children();
            addResourceReferences(children, references);
        }
        return references;
    }

    private void addResourceReferences(List<Property> children, HashSet<String> references) {
        for (Property child : children) {
            if(child.hasValues()) {
                for (Element element : child.getValues()) {
                    if((element instanceof ResourceReference)) {
                        references.add(((ResourceReference) element).getReferenceSimple());
                    } else {
                        addResourceReferences(element.children(), references);
                    }
                }
            }
        }
    }

    public EncounterReference getEncounterReference() {
        return encounterReference;
    }

    public PatientReference getPatientReference() {
        return patientReference;
    }

    public BundleContext getContext() {
        return context;
    }


    public ServiceProviderReference getServiceProviderReference() {
        return serviceProviderReference;
    }

    public Composition getComposition() {
        return composition;
    }
}
