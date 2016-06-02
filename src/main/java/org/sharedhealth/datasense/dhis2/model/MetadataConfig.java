package org.sharedhealth.datasense.dhis2.model;

public class MetadataConfig {
    private String facilityId;

    public MetadataConfig() {
    }

    public MetadataConfig(String facilityId) {
        this.facilityId = facilityId;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }
}
