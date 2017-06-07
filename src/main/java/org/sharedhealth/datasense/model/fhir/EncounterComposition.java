package org.sharedhealth.datasense.model.fhir;


import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.*;

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
        patientReference = new PatientReference(encounterReference.getResource().getSubject());
        serviceProviderReference = new ServiceProviderReference();
        Reference serviceProvider = encounterReference.getResource().getServiceProvider();
        if (serviceProvider != null) {
            serviceProviderReference.setReference(serviceProvider);
        }
        providerReference = new ProviderReference();
        List<Encounter.EncounterParticipantComponent> participants = encounterReference.getResource().getParticipant();
        if (!participants.isEmpty()) {
            for (Encounter.EncounterParticipantComponent participant : participants) {
                providerReference.addReference(participant.getIndividual());
            }
        }
    }

    public ArrayList<Resource> getCompositionRefResources() {
        ArrayList<Resource> resources = new ArrayList<>();
        for (Composition.SectionComponent section : composition.getSection()) {
            List<Reference> entry = section.getEntry();
            if (entry.isEmpty()) {
                break;
            }
            Resource resourceForReference = context.getResourceForReference(entry.get(0));
            if (!(resourceForReference instanceof Encounter)) {
                resources.add(resourceForReference);
            }
        }
        return resources;
    }

    public List<Resource> getTopLevelResources() {
        return identifyTopLevelResourcesByExclusion();
    }

    private List<Resource> identifyTopLevelResourcesByExclusion() {
        ArrayList<Resource> compositionRefResources = getCompositionRefResources();
        HashSet<Reference> childRef = getChildReferences(compositionRefResources);

        List<Resource> topLevelResources = new ArrayList<>();

        for (Resource compositionRefResource : compositionRefResources) {
            if (!isChildReference(childRef, compositionRefResource.getId())) {
                topLevelResources.add(compositionRefResource);
            }
        }
        return topLevelResources;
        //        List<Reference> childResourceReferences = new ArrayList<>();
        //        for (Resource compositionRefResource : compositionRefResources) {
        //             childResourceReferences.addAll(compositionRefResource.getAllPopulatedChildElementsOfType(Reference.class));
        //        }
        //        HashSet<Reference> childRef = new HashSet<>();
        //        childRef.addAll(childResourceReferences);
        //
        //        ArrayList<Resource> topLevelResources = new ArrayList<>();
        //
        //        for (Resource compositionRefResource : compositionRefResources) {
        //            if(!isChildReference(childRef, compositionRefResource.getId())) {
        //                topLevelResources.add(compositionRefResource);
        //            }
        //        }
        //        return topLevelResources;
    }

    private static HashSet<Reference> getChildReferences(List<Resource> compositionRefResources) {
        List<Reference> childResourceReferences = new ArrayList<>();
        for (Resource compositionRefResource : compositionRefResources) {
            // add all observation as part of observation target
            // add all observation as part of diagnosticreport result
            // add all diagnostic reports as part of procedure.report
            // add all medication requests as part of medicationrequest.priorprescription
            if (compositionRefResource instanceof DiagnosticReport) {
                DiagnosticReport diagnosticReport = (DiagnosticReport) compositionRefResource;
                childResourceReferences.addAll(diagnosticReport.getResult());
            }
            if (compositionRefResource instanceof MedicationRequest) {
                MedicationRequest medicationRequest = (MedicationRequest) compositionRefResource;
                Reference priorPrescription = medicationRequest.getPriorPrescription();
                if (!priorPrescription.isEmpty()) {
                    childResourceReferences.add(priorPrescription);
                }
            }
            if (compositionRefResource instanceof Procedure) {
                Procedure procedure = (Procedure) compositionRefResource;
                childResourceReferences.addAll(procedure.getReport());
            }
            if (compositionRefResource instanceof Observation) {
                List<Observation.ObservationRelatedComponent> related = ((Observation) compositionRefResource).getRelated();
                for (Observation.ObservationRelatedComponent observationRelatedComponent : related) {
                    childResourceReferences.add(observationRelatedComponent.getTarget());
                }
            }
        }
        HashSet<Reference> childRef = new HashSet<>();
        childRef.addAll(childResourceReferences);
        return childRef;
    }

    private boolean isChildReference(HashSet<Reference> childReferenceDts, String resourceRef) {
        for (Reference childRef : childReferenceDts) {
            if (StringUtils.isNotBlank(childRef.getReference()) && childRef.getReference().equals(resourceRef)) {
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

    public Resource getResourceByReference(Reference resultReerence) {
        return context.getResourceForReference(resultReerence);
    }
}
