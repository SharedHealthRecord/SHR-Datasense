package org.sharedhealth.datasense.service;

import static org.sharedhealth.datasense.util.SchedulerConstants.DAILY_JOB_PARAM_KEY;
import static org.sharedhealth.datasense.util.SchedulerConstants.MONTHLY_JOB_PARAM_KEY;

public class JobAttributes {
    private String name;
    private String cronExpression;
    private String paramKey;
    private String paramValue;

    public JobAttributes(String name) {
        this.name = name;
        setParamKey();
    }

    public JobAttributes(String name, String cronExpression, String paramValue) {
        this.name = name;
        this.cronExpression = cronExpression;
        this.paramValue = paramValue;
        setParamKey();
    }

    private void setParamKey() {
        if (this.name.startsWith("DAILY-")) {
            this.paramKey = DAILY_JOB_PARAM_KEY;
        } else if (this.name.startsWith("MONTHLY-")) {
            this.paramKey = MONTHLY_JOB_PARAM_KEY;
        }
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
