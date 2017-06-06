package org.sharedhealth.datasense.model.fhir;

import org.hl7.fhir.dstu3.model.Reference;
import org.sharedhealth.datasense.model.Patient;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;

public class PatientReference {
    private Reference subject;
    private Patient value;

    public PatientReference(Reference subject) {
        this.subject = subject;
    }

    public String getHealthId() {
        return substringAfterLast(subject.getReference(), "/");
    }

    public void setValue(Patient patient) {
        this.value = patient;
    }

    public Patient getValue() {
        return value;
    }
}
