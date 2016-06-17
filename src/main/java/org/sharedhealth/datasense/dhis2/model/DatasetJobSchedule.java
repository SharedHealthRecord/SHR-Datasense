package org.sharedhealth.datasense.dhis2.model;

public class DatasetJobSchedule {

    private String datasetName;
    private String nextFireTime;
    private String prevFireTime;
    private String startTime;
    private String endTime;
    private String facilityId;
    private String previousPeriod;
    private String nextPeriod;
    private String facilityName;

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setNextFireTime(String nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public String getNextFireTime() {
        return nextFireTime;
    }

    public void setPrevFireTime(String prevFireTime) {
        this.prevFireTime = prevFireTime;
    }

    public String getPrevFireTime() {
        return prevFireTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public void setPreviousPeriod(String period) {
        this.previousPeriod = period;
    }

    public String getPreviousPeriod() {
        return previousPeriod;
    }

    public void setNextPeriod(String nextPeriod) {
        this.nextPeriod = nextPeriod;
    }

    public String getNextPeriod() {
        return nextPeriod;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getFacilityName() {
        return facilityName;
    }
}
