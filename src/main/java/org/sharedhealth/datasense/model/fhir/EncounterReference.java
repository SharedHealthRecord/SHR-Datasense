package org.sharedhealth.datasense.model.fhir;

import org.sharedhealth.datasense.model.Encounter;

public class EncounterReference {
    private Encounter value;
    private ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt encounterReference;
    private ca.uhn.fhir.model.dstu2.resource.Encounter resource;

    public EncounterReference(ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt encounterReference,
                              ca.uhn.fhir.model.dstu2.resource.Encounter encounter) {
        this.encounterReference = encounterReference;
        this.resource = encounter;
    }

    public void setValue(Encounter value) {
        this.value = value;
    }

    public Encounter getValue() {
        return value;
    }

    public String getEncounterId(){
        return this.getValue().getEncounterId();
    }

    public ca.uhn.fhir.model.dstu2.resource.Encounter getResource() {
        return resource;
    }
}
