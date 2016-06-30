package org.sharedhealth.datasense.model;

import java.util.Date;

public class DiagnosticOrder extends BaseResource {
    private String patientHid;
    private String encounterId;
    private Date orderDate;
    private String orderCategory;
    private String code;
    private String orderer;
    private String orderConcept;
    private String orderStatus;
    private Integer id;
    private String shrOrderUuid;

    public String getShrOrderUuid() {
        return shrOrderUuid;
    }

    public void setShrOrderUuid(String shrOrderUuid) {
        this.shrOrderUuid = shrOrderUuid;
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

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderCategory() {
        return orderCategory;
    }

    public void setOrderCategory(String orderCategory) {
        this.orderCategory = orderCategory;
    }

    public String getCode() {
        return code;
    }

    public void setcode(String code) {
        this.code = code;
    }

    public String getOrderer() {
        return orderer;
    }

    public void setOrderer(String orderer) {
        this.orderer = orderer;
    }

    public String getOrderConcept() {
        return orderConcept;
    }

    public void setOrderConcept(String orderConcept) {
        this.orderConcept = orderConcept;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
