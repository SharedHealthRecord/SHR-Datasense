package org.sharedhealth.datasense.model;

public class ImmunizationReason extends BaseResource {
    private String code;
    private String descr;
    private String incidentUuid;
    private String encounterId;
    private String hid;

    public ImmunizationReason() {
    }


    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getDescr() {
        return descr;
    }

    public void setIncidentUuid(String incidentUuid) {
        this.incidentUuid = incidentUuid;
    }

    public String getIncidentUuid() {
        return incidentUuid;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public String getEncounterId() {
        return encounterId;
    }

    public void setHid(String hid) {
        this.hid = hid;
    }

    public String getHid() {
        return hid;
    }
}
