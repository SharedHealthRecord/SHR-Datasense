package org.sharedhealth.datasense.export.dhis;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sharedhealth.datasense.export.dhis.report.DHISMonthlyEPIInfantReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class DHISMonthlyEPIInfantPostJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(DHISMonthlyEPIInfantPostJob.class);

    public void setDhisMonthlyEPIInfantReport(DHISMonthlyEPIInfantReport dhisMonthlyEPIInfantReport) {
        this.dhisMonthlyEPIInfantReport = dhisMonthlyEPIInfantReport;
    }

    private DHISMonthlyEPIInfantReport dhisMonthlyEPIInfantReport;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String reportingMonthParam = (String) context.getMergedJobDataMap().get("reportingMonth");
        System.out.println(reportingMonthParam);

        String reportingMonth = getReportingMonth(reportingMonthParam);
        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("reportingMonth", reportingMonth);
        dhisMonthlyEPIInfantReport.process(dataMap);
    }

    static String getReportingMonth(String monthParam) {
        try {
            new SimpleDateFormat("yyyy-MM").parse(monthParam);
            return monthParam;
        } catch (ParseException e) {
            logger.error(String.format("Invalid argument [%s] for reportingMonth Parameter. Expected format yyyy-MM. Trying to parse as Integer .. ", monthParam));
        }

        Integer addMonth = null;
        try {
            addMonth = Integer.parseInt(monthParam);
        } catch (NumberFormatException e) {
            logger.error(String.format("Invalid argument for reportingMonth Parameter. Actual [%s] expected values are integer (-1, -2 etc). Defaulting to last month", monthParam));
        }

        if (addMonth == null) {
            addMonth = -1;
        }
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, addMonth);
        return new SimpleDateFormat("yyyy-MM").format(c.getTime());
    }
}
