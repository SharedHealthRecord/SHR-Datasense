package org.sharedhealth.datasense.model;

import java.util.Date;

public class Observation extends BaseResource {
    private Integer observationId;
    private Patient patient;
    private Encounter encounter;
    private String conceptId;
    private String referenceCode;
    private Date datetime;
    private String parentId;
    private String value;

    public void setObservationId(int observationId) {
        this.observationId = observationId;
    }

    public Integer getObservationId() {
        return observationId;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
    }

    public Encounter getEncounter() {
        return encounter;
    }

    public void setConceptId(String conceptId) {
        this.conceptId = conceptId;
    }

    public String getConceptId() {
        return conceptId;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public Date getDateTime() {
        return datetime;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
