package org.sharedhealth.datasense.export.dhis.Jobs;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sharedhealth.datasense.util.SchedulerConstants;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.sharedhealth.datasense.util.SchedulerConstants.MONTHLY_JOB_PARAM_KEY;

public abstract class MonthlyJob extends QuartzJobBean {

    private static final Logger logger = Logger.getLogger(MonthlyJob.class);
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String reportingMonthParam = (String) context.getMergedJobDataMap().get(MONTHLY_JOB_PARAM_KEY);

        String reportingMonth = getReportingMonth(reportingMonthParam);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("reportingMonth", reportingMonth);
        process(dataMap);
    }

    protected abstract void process(Map<String, Object> dataMap);

    public static String getReportingMonth(String monthParam) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
        try {
            return dateFormat.format(dateFormat.parse(monthParam));
        } catch (ParseException e) {
            logger.debug(String.format("Invalid argument [%s] for reportingMonth Parameter. Expected format yyyy-MM. " +
                    "Trying to parse as Integer .. ", monthParam));
        }

        Integer addMonth = null;
        try {
            addMonth = Integer.parseInt(monthParam);
        } catch (NumberFormatException e) {
            logger.debug(String.format("Invalid argument for reportingMonth Parameter. Actual [%s] expected values " +
                    "are integer (-1, -2 etc). Defaulting to last month", monthParam));
        }

        if (addMonth == null) {
            addMonth = -1;
        }
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, addMonth);
        return dateFormat.format(c.getTime());
    }
}
