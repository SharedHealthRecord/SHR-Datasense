package org.sharedhealth.datasense.service;

public class JobAttributes {
    private String name;
    private String cronExpression;
    private String paramKey;
    private String paramValue;

    public JobAttributes(String name) {
        this(name, null, null, null);
    }

    public JobAttributes(String name, String cronExpression, String paramKey, String paramValue) {
        this.name = name;
        this.cronExpression = cronExpression;
        this.paramKey = paramKey;
        this.paramValue = paramValue;
    }

    public String getName() {
        return name;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public String getParamKey() {
        return paramKey;
    }

    public String getParamValue() {
        return paramValue;
    }
}
