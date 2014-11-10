package org.sharedhealth.datasense.model.fhir;

import org.hl7.fhir.instance.model.ResourceReference;
import org.sharedhealth.datasense.model.Encounter;

public class EncounterReference {
    private Encounter value;
    private ResourceReference encounterReference;
    private org.hl7.fhir.instance.model.Encounter encounterReferenceValue;

    public EncounterReference(ResourceReference encounterReference, org.hl7.fhir.instance.model.Encounter encounter) {
        this.encounterReference = encounterReference;
        this.encounterReferenceValue = encounter;
    }

    public void setValue(Encounter value) {
        this.value = value;
    }

    public Encounter getValue() {
        return value;
    }

    public ResourceReference getEncounterReference() {
        return encounterReference;
    }

    public org.hl7.fhir.instance.model.Encounter getEncounterReferenceValue() {
        return encounterReferenceValue;
    }
}
