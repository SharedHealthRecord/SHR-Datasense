package org.sharedhealth.datasense.export.dhis.Jobs;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sharedhealth.datasense.export.dhis.reports.DHISDynamicReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class DHISQuartzJob extends QuartzJobBean {

    @Autowired
    private DHISDynamicReport dhisDynamicReport;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap mergedJobDataMap = context.getMergedJobDataMap();
        dhisDynamicReport.processAndPost(mergedJobDataMap);
    }


}
