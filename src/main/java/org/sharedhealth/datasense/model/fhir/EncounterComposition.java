package org.sharedhealth.datasense.model.fhir;

import org.hl7.fhir.instance.model.Composition;
import org.hl7.fhir.instance.model.Encounter;

public class EncounterComposition {
    private final Composition composition;
    private final BundleContext context;
    private final PatientReference patientReference;
    private Encounter encounter;

    public EncounterComposition(Composition composition, BundleContext context) {
        this.composition = composition;
        this.context = context;
        encounter = (Encounter) context.getResourceByReference(composition.getEncounter());
        patientReference = new PatientReference(encounter.getSubject());
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
}
