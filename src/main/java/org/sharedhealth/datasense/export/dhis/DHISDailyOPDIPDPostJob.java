package org.sharedhealth.datasense.export.dhis;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class DHISDailyOPDIPDPostJob extends QuartzJobBean{
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        System.out.println("******************************");
        System.out.println("Posting to DHIS");
        System.out.println("******************************");
    }
}
