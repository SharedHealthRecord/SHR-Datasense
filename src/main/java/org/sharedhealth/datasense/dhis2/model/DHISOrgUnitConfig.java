package org.sharedhealth.datasense.dhis2.model;

public class DHISOrgUnitConfig {
    private String id;

    private String facilityId;
    private String facilityName;

    private String orgUnitId;
    private String orgUnitName;

    public DHISOrgUnitConfig() {
    }

    public DHISOrgUnitConfig(String name, String id) {
        this.facilityName = name;
        this.facilityId = id;
    }

    public DHISOrgUnitConfig(String facilityId, String facilityName, String orgUnitId, String orgUnitName) {
        this.facilityId = facilityId;
        this.facilityName = facilityName;
        this.orgUnitId = orgUnitId;
        this.orgUnitName = orgUnitName;
    }

    public DHISOrgUnitConfig(String id, String facilityId, String facilityName, String orgUnitId, String orgUnitName) {
        this.id = id;
        this.facilityId = facilityId;
        this.facilityName = facilityName;
        this.orgUnitId = orgUnitId;
        this.orgUnitName = orgUnitName;
    }

    public String getFacilityName() {
        return facilityName;
    }
    public String getFacilityId() {
        return facilityId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getOrgUnitId() {
        return orgUnitId;
    }

    public void setOrgUnitId(String orgUnitId) {
        this.orgUnitId = orgUnitId;
    }

    public String getOrgUnitName() {
        return orgUnitName;
    }

    public void setOrgUnitName(String orgUnitName) {
        this.orgUnitName = orgUnitName;
    }
}
