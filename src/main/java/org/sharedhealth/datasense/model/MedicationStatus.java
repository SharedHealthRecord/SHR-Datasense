package org.sharedhealth.datasense.model;

public enum MedicationStatus {
    Administered("A"),
    Ordered("O"),
    AdministeredImmunization("AI");

    private String value;

    MedicationStatus(String value) {
        this.value = value;
    }

    public static MedicationStatus getMedicationStatus(String value) {
        if (value.equals("A")) return Administered;
        if (value.equals("O")) return Ordered;
        if (value.equals("AI")) return AdministeredImmunization;
        return null;
    }

    public String getValue() {
        return value;
    }
}
