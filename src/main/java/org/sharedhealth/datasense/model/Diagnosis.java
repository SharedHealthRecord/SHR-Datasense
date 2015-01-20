package org.sharedhealth.datasense.model;

import java.util.Date;

public class Diagnosis extends BaseResource {
    private int diagnosisId;
    private Patient patient;
    private Encounter encounter;
    private Date diagnosisDateTime;
    private String diagnosisConcept;
    private String diagnosisCode;
    private String diagnosisStatus;

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
    }

    public void setDiagnosisDateTime(Date diagnosisDateTime) {
        this.diagnosisDateTime = diagnosisDateTime;
    }

    public Patient getPatient() {
        return patient;
    }

    public Encounter getEncounter() {
        return encounter;
    }

    public Date getDiagnosisDateTime() {
        return diagnosisDateTime;
    }

    public int getDiagnosisId() {
        return diagnosisId;
    }

    public void setDiagnosisId(int diagnosisId) {
        this.diagnosisId = diagnosisId;
    }

    public void setDiagnosisConceptId(String diagnosisConcept) {
        this.diagnosisConcept = diagnosisConcept;
    }

    public String getDiagnosisConcept() {
        return diagnosisConcept;
    }

    public void setDiagnosisCode(String diagnosisCode) {
        this.diagnosisCode = diagnosisCode;
    }

    public String getDiagnosisCode() {
        return diagnosisCode;
    }

    public void setDiagnosisStatus(String diagnosisStatus) {
        this.diagnosisStatus = diagnosisStatus;
    }

    public String getDiagnosisStatus() {
        return diagnosisStatus;
    }
}
