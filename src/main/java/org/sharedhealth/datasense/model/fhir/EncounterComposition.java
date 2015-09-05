package org.sharedhealth.datasense.model.fhir;


import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Composition;
import ca.uhn.fhir.model.dstu2.resource.Encounter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class EncounterComposition {
    private final Composition composition;
    private final BundleContext context;
    private final PatientReference patientReference;
    private EncounterReference encounterReference;
    private ServiceProviderReference serviceProviderReference;
    private ProviderReference providerReference;

    public EncounterComposition(Composition composition, BundleContext context) {
        this.composition = composition;
        this.context = context;
        Encounter encounter = (Encounter) context.getResourceForReference(composition.getEncounter());
        encounterReference = new EncounterReference(composition.getEncounter(), encounter);
        patientReference = new PatientReference(encounterReference.getResource().getPatient());
        serviceProviderReference = new ServiceProviderReference();
        ResourceReferenceDt serviceProvider = encounterReference.getResource().getServiceProvider();
        if (serviceProvider != null) {
            serviceProviderReference.setReference(serviceProvider);
        }
        providerReference = new ProviderReference();
        List<Encounter.Participant> participants = encounterReference.getResource().getParticipant();
        if(!participants.isEmpty()) {
            for (Encounter.Participant participant : participants) {
                providerReference.addReference(participant.getIndividual());
            }
        }
    }

    public ArrayList<IResource> getCompositionRefResources() {
        ArrayList<IResource> resources = new ArrayList<>();
        for (Composition.Section section : composition.getSection()) {
            IResource resourceForReference = context.getResourceForReference(section.getContent());
            if (!(resourceForReference instanceof Encounter)) {
                resources.add(resourceForReference);
            }
        }
        return resources;
    }

    public List<IResource> getTopLevelResources() {
        return identifyTopLevelResourcesByExclusion();
    }

    public ArrayList<IResource> getOnlyParents() {
        ArrayList<IResource> compositionRefResources = getCompositionRefResources();
        for (IResource compositionRefResource : compositionRefResources) {
            //
        }
        return null;
    }



    private List<IResource> identifyTopLevelResourcesByExclusion() {
        ArrayList<IResource> compositionRefResources = getCompositionRefResources();
        List<ResourceReferenceDt> childResourceReferences = new ArrayList<>();
        for (IResource compositionRefResource : compositionRefResources) {
             childResourceReferences.addAll(compositionRefResource.getAllPopulatedChildElementsOfType(ResourceReferenceDt.class));
        }
        HashSet<ResourceReferenceDt> childRef = new HashSet<>();
        childRef.addAll(childResourceReferences);

        ArrayList<IResource> topLevelResources = new ArrayList<>();

        for (IResource compositionRefResource : compositionRefResources) {
            if(!isChildReference(childRef, compositionRefResource.getId().getValue())) {
                topLevelResources.add(compositionRefResource);
            }
        }
        return topLevelResources;
    }

    private boolean isChildReference(HashSet<ResourceReferenceDt> childReferenceDts, String resourceRef) {
        for (ResourceReferenceDt childRef : childReferenceDts) {
            if(!childRef.getReference().isEmpty() && childRef.getReference().getValue().equals(resourceRef)) {
                return true;
            }
        }
        return false;
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

    public ProviderReference getProviderReference() {
        return providerReference;
    }

    public ServiceProviderReference getServiceProviderReference() {
        return serviceProviderReference;
    }

    public Composition getComposition() {
        return composition;
    }

    public ArrayList<IResource> getParentResources(String referenceId) {
        ArrayList<IResource> refResources = getCompositionRefResources();
        ArrayList<IResource> parentRefs = new ArrayList<>();
        for (IResource refResource : refResources) {
            if (!refResource.getId().getValue().equals(referenceId)) {
                List<ResourceReferenceDt> childReferences = refResource.getAllPopulatedChildElementsOfType(ResourceReferenceDt.class);
                for (ResourceReferenceDt childReference : childReferences) {
                    if (childReference.getReference().getValue().equals(referenceId)) {
                        parentRefs.add(refResource);
                    }
                }
            }
        }
        return parentRefs;
    }

////    private ArrayList<AtomEntry<? extends Resource>> loadAtomEntriesFromComposition() {
////        ArrayList<AtomEntry<? extends Resource>> atomEntries = new ArrayList<>();
////        for (Composition.SectionComponent sectionComponent : composition.getSection()) {
////            if (!sectionComponent.getContent().getDisplaySimple().equalsIgnoreCase("encounter")) {
////                atomEntries.add(context.getAtomEntryFromFeed(sectionComponent.getContent()));
////            }
////        }
////        return atomEntries;
////    }
////
//    private ArrayList<IResource> getTopLevelResources(HashSet<String> references) {
//        ArrayList<IResource> parentResources = new ArrayList<>();
//        for (AtomEntry<? extends Resource> atomEntry : loadAtomEntriesFromComposition()) {
//            if(!references.contains(atomEntry.getId())) {
//                parentResources.add(atomEntry.getResource());
//            }
//        }
//        return parentResources;
//    }

//    private HashSet<String> getChildResourceReferences() {
//        HashSet<String> references = new HashSet<>();
//        for (Resource resource : getCompositionRefResources()) {
//            List<Property> children = resource.children();
//            addResourceReferences(children, references);
//        }
//        return references;
//    }

//    private void addResourceReferences(List<Property> children, HashSet<String> references) {
//        for (Property child : children) {
//            if(child.hasValues()) {
//                for (Element element : child.getValues()) {
//                    if((element instanceof ResourceReference)) {
//                        references.add(((ResourceReference) element).getReferenceSimple());
//                    } else {
//                        addResourceReferences(element.children(), references);
//                    }
//                }
//            }
//        }
//    }
}
