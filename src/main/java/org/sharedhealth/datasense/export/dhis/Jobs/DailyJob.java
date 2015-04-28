package org.sharedhealth.datasense.export.dhis.Jobs;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sharedhealth.datasense.util.SchedulerConstants;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.sharedhealth.datasense.util.SchedulerConstants.DAILY_JOB_PARAM_KEY;

public abstract class DailyJob extends QuartzJobBean {

    private static final Logger logger = Logger.getLogger(DailyJob.class);

    protected abstract void process(Map<String, Object> dataMap);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        Object reportingDateParam = context.getMergedJobDataMap().get(DAILY_JOB_PARAM_KEY);

        String reportingDate = reportingDateParam != null ? getReportingDate((String) reportingDateParam) :
                getDefaultReportingDate();
        Map<String, Object> map = context.getMergedJobDataMap();
        map.put("reportingDate", reportingDate);
        logger.info(String.format("Generating Report for date %s", reportingDate));
        process(map);
    }

    public static String getReportingDate(String dateParam) {
        try {
            new SimpleDateFormat("yyyy-MM-dd").parse(dateParam);
            return dateParam;
        } catch (ParseException e) {
            logger.debug(String.format("Invalid argument [%s] for reportingParam. Expected date format yyyy-MM-dd. " +
                    "Trying to parse as Integer .. ", dateParam));
        }

        Integer addDays = null;
        try {
            addDays = Integer.parseInt(dateParam);
        } catch (NumberFormatException e) {
            logger.debug(String.format("Invalid argument for reportingParam. Actual [%s] expected values are integer " +
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
