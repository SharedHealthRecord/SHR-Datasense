package org.sharedhealth.datasense.model.fhir;

import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import org.sharedhealth.datasense.model.Patient;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;

public class PatientReference {
    private ResourceReferenceDt subject;
    private Patient value;

    public PatientReference(ResourceReferenceDt subject) {
        this.subject = subject;
    }

    public String getHealthId() {
        return substringAfterLast(subject.getReference().getValue(), "/");
    }

    public void setValue(Patient patient) {
        this.value = patient;
    }

    public Patient getValue() {
        return value;
    }
}
