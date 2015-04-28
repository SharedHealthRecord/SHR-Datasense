package org.sharedhealth.datasense.util;

import java.util.List;

import static java.util.Arrays.asList;

public class SchedulerConstants {
    public static final String DAILY_OPD_IPD_JOB = "DAILY-OPD-IPD-JOB";
    public static final String MONTHLY_EPI_INFANT_JOB = "MONTHLY-EPI-INFANT-JOB";
    public static final String MONTHLY_COLPOSCOPY_JOB = "MONTHLY-COLPOSCOPY-JOB";
    public static final List<String> ALL_JOB_NAMES = asList(DAILY_OPD_IPD_JOB, MONTHLY_EPI_INFANT_JOB, MONTHLY_COLPOSCOPY_JOB);

    public static final String NO_SUCH_REPORT_MESSAGE = "There are no such reports";
    public static final String DAILY_JOB_PARAM_KEY = "reportingDate";
    public static final String MONTHLY_JOB_PARAM_KEY = "reportingMonth";
}
