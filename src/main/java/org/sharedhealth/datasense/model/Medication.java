package org.sharedhealth.datasense.model;

import java.util.Date;

public class Medication {
    private Date medicationDate;
    private Encounter encounter;
    private MedicationStatus status;

    public Encounter getEncounter() {
        return encounter;
    }

    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
    }

    public Medication() {
    }

    public Date getMedicationDate() {
        return medicationDate;
    }

    public void setMedicationDate(Date medicationDate) {
        this.medicationDate = medicationDate;
    }

    public MedicationStatus getStatus() {
        return status;
    }

    public void setStatus(MedicationStatus status) {
        this.status = status;
    }
}
