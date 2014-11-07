package org.sharedhealth.datasense.model.fhir;

import org.hl7.fhir.instance.model.Composition;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class EncounterComposition {
    private final Composition composition;
    private final BundleContext context;
    private final PatientReference patientReference;
    private final ServiceProviderReference serviceProviderReference;
    private Encounter encounter;
    private List<Resource> resources;

    public EncounterComposition(Composition composition, BundleContext context) {
        this.composition = composition;
        this.context = context;
        encounter = (Encounter) context.getResourceByReference(composition.getEncounter());
        patientReference = new PatientReference(encounter.getSubject());
        serviceProviderReference = new ServiceProviderReference(encounter.getServiceProvider());
        findResourcesFromComposition();
    }

    private void findResourcesFromComposition() {
        resources = new ArrayList<>();
        for (Composition.SectionComponent sectionComponent : composition.getSection()) {
            if(!sectionComponent.getContent().getDisplaySimple().equalsIgnoreCase("encounter")) {
                resources.add(context.getResourceByReference(sectionComponent.getContent()));
            }
        }
    }

    public Encounter getEncounter() {
        return encounter;
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

    public List<Resource> getResources() {
        return resources;
    }
}
