package org.sharedhealth.datasense.model;

public enum MedicationStatus {
    Administered("A"),
    Ordered("O"),
    ImmunizationAdministered("IA"),
    ImmunizationRefused("IR"),
    Unknown("U");

    private String value;

    MedicationStatus(String value) {
        this.value = value;
    }

    public static MedicationStatus getMedicationStatus(String value) {
        if (value.equals("A")) return Administered;
        if (value.equals("O")) return Ordered;
        if (value.equals("IA")) return ImmunizationAdministered;
        if (value.equals("IR")) return ImmunizationRefused;
        return Unknown;
    }

    public String getValue() {
        return value;
    }
}
