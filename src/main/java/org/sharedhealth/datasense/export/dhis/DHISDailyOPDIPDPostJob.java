package org.sharedhealth.datasense.export.dhis;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sharedhealth.datasense.export.dhis.annotations.DhisParam;
import org.sharedhealth.datasense.export.dhis.report.DHISDailyOPDIPDReport;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

@DhisParam({"reportingDate"})
public class DHISDailyOPDIPDPostJob extends QuartzJobBean{
    private DHISDailyOPDIPDReport dhisDailyOPDIPDReport;

    public void setDhisDailyOPDIPDReport(DHISDailyOPDIPDReport dhisDailyOPDIPDReport) {
        this.dhisDailyOPDIPDReport = dhisDailyOPDIPDReport;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        Object reportingDateParam = context.getMergedJobDataMap().get("reportingDate");

        String reportingDate = reportingDateParam != null ? getReportingDate((String) reportingDateParam) : getDefaultReportingDate();
        Map map = context.getMergedJobDataMap();
        map.put("reportingDate", reportingDate);
        System.out.println("******************************");
        System.out.println("Posting to DHIS");
        System.out.println("******************************");
        dhisDailyOPDIPDReport.process(map);
    }

    private String getReportingDate(String dateParam) {
        if (dateParam.endsWith("default")) {
            return getDefaultReportingDate();
        }
        return dateParam;
    }

    private String getDefaultReportingDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1);
        return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
    }
}
