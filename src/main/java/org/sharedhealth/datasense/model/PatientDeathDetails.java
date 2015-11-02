package org.sharedhealth.datasense.model;

import java.util.Date;

public class PatientDeathDetails extends BaseResource {
    private Patient patient;
    private Encounter encounter;
    private Date dateOfDeath;
    private String causeOfDeathCode;
    private String causeOfDeathConceptUuid;
    private String circumstancesOfDeathUuid;
    private String circumstancesOfDeathCode;
    private String placeOfDeathUuid;
    private String placeOfDeathCode;

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

    public void setCircumstancesOfDeathUuid(String circumstancesOfDeathUuid) {
        this.circumstancesOfDeathUuid = circumstancesOfDeathUuid;
    }

    public String getCircumstancesOfDeathUuid() {
        return circumstancesOfDeathUuid;
    }

    public void setCircumstancesOfDeathCode(String circumstancesOfDeathCode) {
        this.circumstancesOfDeathCode = circumstancesOfDeathCode;
    }

    public String getCircumstancesOfDeathCode() {
        return circumstancesOfDeathCode;
    }

    public void setPlaceOfDeathUuid(String placeOfDeathUuid) {
        this.placeOfDeathUuid = placeOfDeathUuid;
    }

    public String getPlaceOfDeathUuid() {
        return placeOfDeathUuid;
    }

    public void setPlaceOfDeathCode(String placeOfDeathCode) {
        this.placeOfDeathCode = placeOfDeathCode;
    }

    public String getPlaceOfDeathCode() {
        return placeOfDeathCode;
    }
}
