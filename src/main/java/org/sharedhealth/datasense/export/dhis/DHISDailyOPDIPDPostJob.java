package org.sharedhealth.datasense.export.dhis;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sharedhealth.datasense.export.dhis.report.DHISDailyOPDIPDReport;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class DHISDailyOPDIPDPostJob extends QuartzJobBean{
    private DHISDailyOPDIPDReport dhisDailyOPDIPDReport;

    public void setDhisDailyOPDIPDReport(DHISDailyOPDIPDReport dhisDailyOPDIPDReport) {
        this.dhisDailyOPDIPDReport = dhisDailyOPDIPDReport;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        System.out.println("******************************");
        System.out.println("Posting to DHIS");
        System.out.println("******************************");
        dhisDailyOPDIPDReport.process();
    }
}
