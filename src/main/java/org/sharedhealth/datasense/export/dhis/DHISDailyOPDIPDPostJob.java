package org.sharedhealth.datasense.export.dhis;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sharedhealth.datasense.export.dhis.annotations.DhisParam;
import org.sharedhealth.datasense.export.dhis.report.DHISDailyOPDIPDReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

@DhisParam({"reportingDate"})
public class DHISDailyOPDIPDPostJob extends QuartzJobBean {
    private DHISDailyOPDIPDReport dhisDailyOPDIPDReport;

    private static final Logger logger = LoggerFactory.getLogger(DHISDailyOPDIPDPostJob.class);


    public void setDhisDailyOPDIPDReport(DHISDailyOPDIPDReport dhisDailyOPDIPDReport) {
        this.dhisDailyOPDIPDReport = dhisDailyOPDIPDReport;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        Object reportingDateParam = context.getMergedJobDataMap().get("reportingDate");

        String reportingDate = reportingDateParam != null ? getReportingDate((String) reportingDateParam) :
                getDefaultReportingDate();
        Map<String, Object> map = context.getMergedJobDataMap();
        map.put("reportingDate", reportingDate);
        logger.info(String.format("Generating Report for date %s", reportingDate));
        dhisDailyOPDIPDReport.process(map);
    }

    static String getReportingDate(String dateParam) {
        try {
            new SimpleDateFormat("yyyy-MM-dd").parse(dateParam);
            return dateParam;
        } catch (ParseException e) {
            logger.error(String.format("Invalid argument [%s] for reportingParam. Expected date format yyyy-MM-dd. " +
                    "Trying to parse as Integer .. ", dateParam));
        }

        Integer addDays = null;
        try {
            addDays = Integer.parseInt(dateParam);
        } catch (NumberFormatException e) {
            logger.error(String.format("Invalid argument for reportingParam. Actual [%s] expected values are integer " +
                    "(-1, -2 etc). Defaulting to yesterday", dateParam));
        }

        if (addDays == null) {
            addDays = -1;
        }

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, addDays);
        return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
    }

    private String getDefaultReportingDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1);
        return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
    }
}
