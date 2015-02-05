package org.sharedhealth.datasense.model;

import java.util.Date;

public class Procedure extends BaseResource {
    String patientHid;
    String encounterId;
    Date date;
    Date startDate;
    Date endDate;
    String procedureUuid;
    String procedureCode;
    String diagnosisUuid;
    String diagnosisCode;


    public String getDiagnosisCode() {
        return diagnosisCode;
    }

    public void setDiagnosisCode(String diagnosisCode) {
        this.diagnosisCode = diagnosisCode;
    }

    public String getDiagnosisUuid() {
        return diagnosisUuid;
    }

    public void setDiagnosisUuid(String diagnosisUuid) {
        this.diagnosisUuid = diagnosisUuid;
    }

    public String getProcedureCode() {
        return procedureCode;
    }

    public void setProcedureCode(String procedureCode) {
        this.procedureCode = procedureCode;
    }

    public String getProcedureUuid() {
        return procedureUuid;
    }

    public void setProcedureUuid(String procedureUuid) {
        this.procedureUuid = procedureUuid;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEncounterDate() {
        return date;
    }

    public void setEncounterDate(Date date) {
        this.date = date;
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

}
