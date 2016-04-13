package org.sharedhealth.datasense.model;

import java.util.Date;

public class DiagnosticReport extends BaseResource {
    private Integer reportId;
    private String patientHid;
    private String encounterId;
    private Integer orderId;
    private Date reportDate;
    private String reportCategory;
    private String reportCode;
    private String fulfiller;

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getReportConcept() {
        return reportConcept;
    }

    public void setReportConcept(String reportConcept) {
        this.reportConcept = reportConcept;
    }

    public String getPatientHid() {
        return patientHid;
    }

    public void setPatientHid(String patientHid) {
        this.patientHid = patientHid;
    }

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public String getReportCategory() {
        return reportCategory;
    }

    public void setReportCategory(String reportCategory) {
        this.reportCategory = reportCategory;
    }

    public String getReportCode() {
        return reportCode;
    }

    public void setReportCode(String reportCode) {
        this.reportCode = reportCode;
    }

    public String getFulfiller() {
        return fulfiller;
    }

    public void setFulfiller(String fulfiller) {
        this.fulfiller = fulfiller;
    }

    private String reportConcept;


    public Integer getReportId() {
        return reportId;
    }


    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

}
