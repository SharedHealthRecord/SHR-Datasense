package org.sharedhealth.datasense.model;

import java.util.Date;

public class Medication extends BaseResource {
    private Date dateTime;
    private Encounter encounter;
    private MedicationStatus status;
    private Patient patient;
    private String drugId;

    public Encounter getEncounter() {
        return encounter;
    }

    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
    }

    public Medication() {
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public MedicationStatus getStatus() {
        return status;
    }

    public void setStatus(MedicationStatus status) {
        this.status = status;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Patient getPatient() {
        return patient;
    }

    public String getDrugId() {
        return drugId;
    }

    public void setDrugId(String drugId) {
        this.drugId = drugId;
    }

}
