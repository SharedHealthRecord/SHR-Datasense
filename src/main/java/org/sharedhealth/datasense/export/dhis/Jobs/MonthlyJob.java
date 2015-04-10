package org.sharedhealth.datasense.export.dhis.Jobs;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public abstract class MonthlyJob extends QuartzJobBean {

    private static final Logger logger = Logger.getLogger(MonthlyJob.class);
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String reportingMonthParam = (String) context.getMergedJobDataMap().get("reportingMonth");

        String reportingMonth = getReportingMonth(reportingMonthParam);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("reportingMonth", reportingMonth);
        process(dataMap);
    }

    protected abstract void process(Map<String, Object> dataMap);

    public static String getReportingMonth(String monthParam) {
        try {
            new SimpleDateFormat("yyyy-MM").parse(monthParam);
            return monthParam;
        } catch (ParseException e) {
            logger.error(String.format("Invalid argument [%s] for reportingMonth Parameter. Expected format yyyy-MM. " +
                    "Trying to parse as Integer .. ", monthParam));
        }

        Integer addMonth = null;
        try {
            addMonth = Integer.parseInt(monthParam);
        } catch (NumberFormatException e) {
            logger.error(String.format("Invalid argument for reportingMonth Parameter. Actual [%s] expected values " +
                    "are integer (-1, -2 etc). Defaulting to last month", monthParam));
        }

        if (addMonth == null) {
            addMonth = -1;
        }
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, addMonth);
        return new SimpleDateFormat("yyyy-MM").format(c.getTime());
    }
}
