package org.sharedhealth.datasense.model;

public class Facility {
    private String facilityId;
    private String facilityName;
    private String facilityType;
    private Address facilityLocation;
    private String facilityLocationCode;
    private String dhisOrgUnitUid;

    public Facility(){}

    public Facility(String facilityId, String facilityName, String facilityType, String facilityLocationCode, String dhisOrgUnitUid) {
        this.facilityId = facilityId;
        this.facilityName = facilityName;
        this.facilityType = facilityType;
        this.facilityLocationCode = facilityLocationCode;
        this.dhisOrgUnitUid = dhisOrgUnitUid;
    }

    public String getFacilityType() {
        return facilityType;
    }

    public void setFacilityType(String facilityType) {
        this.facilityType = facilityType;
    }

    public Address getFacilityLocation() {
        return facilityLocation;
    }

    public void setFacilityLocation(Address facilityLocation) {
        this.facilityLocation = facilityLocation;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public String getFacilityLocationCode() {
        if(facilityLocationCode != null) {
            return this.facilityLocationCode;
        } else {
            return facilityLocation.getLocationCode();
        }
    }

    public void setFacilityLocationCode(String facilityLocationCode) {
        this.facilityLocationCode = facilityLocationCode;
    }

    public String getDhisOrgUnitUid() {
        return dhisOrgUnitUid;
    }

    public void setDhisOrgUnitUid(String dhisOrgUnitUid) {
        this.dhisOrgUnitUid = dhisOrgUnitUid;
    }
}