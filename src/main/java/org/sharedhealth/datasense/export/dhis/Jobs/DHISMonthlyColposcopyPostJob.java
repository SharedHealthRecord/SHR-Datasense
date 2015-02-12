package org.sharedhealth.datasense.export.dhis.Jobs;

import org.sharedhealth.datasense.export.dhis.reports.DHISMonthlyColposcopyReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DHISMonthlyColposcopyPostJob extends MonthlyJob {

    private static final Logger logger = LoggerFactory.getLogger(DHISMonthlyColposcopyPostJob.class);
    private DHISMonthlyColposcopyReport dhisMonthlyColposcopyReport;

    public void setDhisMonthlyColposcopyReport(DHISMonthlyColposcopyReport dhisMonthlyColposcopyReport) {
        this.dhisMonthlyColposcopyReport = dhisMonthlyColposcopyReport;
    }

    @Override
    protected void process(Map<String, Object> dataMap) {
        logger.info("Processing ColposcopyPostJob...");
        dhisMonthlyColposcopyReport.process(dataMap);
    }
}