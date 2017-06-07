package org.sharedhealth.datasense.model.fhir;

import org.hl7.fhir.dstu3.model.Reference;
import org.sharedhealth.datasense.model.Encounter;

public class EncounterReference {
    private Encounter value;
    private Reference encounterReference;
    private org.hl7.fhir.dstu3.model.Encounter resource;

    public EncounterReference(Reference encounterReference,
                              org.hl7.fhir.dstu3.model.Encounter encounter) {
        this.encounterReference = encounterReference;
        this.resource = encounter;
    }

    public void setValue(Encounter value) {
        this.value = value;
    }

    public Encounter getValue() {
        return value;
    }

    public String getEncounterId() {
        return this.getValue().getEncounterId();
    }

    public org.hl7.fhir.dstu3.model.Encounter getResource() {
        return resource;
    }
}
