package org.sharedhealth.datasense.model;

import java.util.Date;

public class PrescribedDrug extends BaseResource {
    private String patientHid;
    private String encounterId;
    private Date prescriptionDateTime;
    private String drugUuid;
    private String drugName;
    private String prescriber;

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getDrugUuid() {
        return drugUuid;
    }

    public void setDrugUuid(String drugUuid) {
        this.drugUuid = drugUuid;
    }

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public String getPatientHid() {
        return patientHid;
    }

    public void setPatientHid(String patientHid) {
        this.patientHid = patientHid;
    }

    public Date getPrescriptionDateTime() {
        return prescriptionDateTime;
    }

    public void setPrescriptionDateTime(Date prescriptionDateTime) {
        this.prescriptionDateTime = prescriptionDateTime;
    }

    public String getPrescriber() {
        return prescriber;
    }

    public void setPrescriber(String provider) {
        this.prescriber = provider;
    }
}
