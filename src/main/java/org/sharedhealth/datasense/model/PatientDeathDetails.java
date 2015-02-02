package org.sharedhealth.datasense.model;

import java.util.Date;

public class PatientDeathDetails extends BaseResource {
    private Patient patient;
    private Encounter encounter;
    private Date dateOfDeath;
    private int patientAgeInYears;
    private int patientAgeInMonths;
    private int patientAgeInDays;
    private String circumstancesOfDeath;
    private String causeOfDeathCode;
    private String causeOfDeathConceptUuid;

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }


    public Encounter getEncounter() {
        return encounter;
    }

    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
    }

    public Date getDateOfDeath() {
        return dateOfDeath;
    }

    public void setDateOfDeath(Date dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }

    public int getPatientAgeInYears() {
        return patientAgeInYears;
    }

    public void setPatientAgeInYears(int patientAgeInYears) {
        this.patientAgeInYears = patientAgeInYears;
    }

    public int getPatientAgeInMonths() {
        return patientAgeInMonths;
    }

    public void setPatientAgeInMonths(int patientAgeInMonths) {
        this.patientAgeInMonths = patientAgeInMonths;
    }

    public int getPatientAgeInDays() {
        return patientAgeInDays;
    }

    public void setPatientAgeInDays(int patientAgeInDays) {
        this.patientAgeInDays = patientAgeInDays;
    }

    public String getCircumstancesOfDeath() {
        return circumstancesOfDeath;
    }

    public void setCircumstancesOfDeath(String circumstancesOfDeath) {
        this.circumstancesOfDeath = circumstancesOfDeath;
    }

    public String getCauseOfDeathCode() {
        return causeOfDeathCode;
    }

    public void setCauseOfDeathCode(String causeOfDeathCode) {
        this.causeOfDeathCode = causeOfDeathCode;
    }

    public String getCauseOfDeathConceptUuid() {
        return causeOfDeathConceptUuid;
    }

    public void setCauseOfDeathConceptUuid(String causeOfDeathConceptUuid) {
        this.causeOfDeathConceptUuid = causeOfDeathConceptUuid;
    }
}
