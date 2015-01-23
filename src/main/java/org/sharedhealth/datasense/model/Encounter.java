package org.sharedhealth.datasense.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class Encounter {
    private String encounterId;
    private String encounterType;
    private String encounterVisitType;
    private Facility facility;
    private Patient patient;
    private Date encounterDateTime;
    private String locationCode;
    private int patientAgeInYears;
    private int patientAgeInMonths;
    private int patientAgeInDays;

    public void setEncounterType(String encounterType) {
        this.encounterType = encounterType;
    }

    public String getEncounterType() {
        return encounterType;
    }

    public void setEncounterVisitType(String encounterVisitType) {
        this.encounterVisitType = encounterVisitType;
    }

    public String getEncounterVisitType() {
        return encounterVisitType;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setEncounterDateTime(Date encounterDateTime) {
        this.encounterDateTime = encounterDateTime;
    }

    public Date getEncounterDateTime() {
        return encounterDateTime;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getLocationCode() {
        if (StringUtils.isNotBlank(locationCode)) {
            return locationCode;
        } else if (facility.getFacilityLocationCode() != null) {
            return facility.getFacilityLocationCode();
        }
        return null;
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

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }
}
