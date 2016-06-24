package org.sharedhealth.datasense.model;

import java.util.Date;

public class PrescribedDrug extends BaseResource {
    private String patientHid;
    private String encounterId;
    private Date prescriptionDateTime;
    private String drugUuid;
    private String drugName;
    private String prescriber;
    private String status;
    private String shrMedicationOrderUuid;
    private String priorShrMedicationOrderUuid;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getShrMedicationOrderUuid() {
        return shrMedicationOrderUuid;
    }

    public void setShrMedicationOrderUuid(String shrMedicationOrderUuid) {
        this.shrMedicationOrderUuid = shrMedicationOrderUuid;
    }

    public String getPriorShrMedicationOrderUuid() {
        return priorShrMedicationOrderUuid;
    }

    public void setPriorShrMedicationOrderUuid(String priorShrMedicationOrderUuid) {
        this.priorShrMedicationOrderUuid = priorShrMedicationOrderUuid;
    }
}
