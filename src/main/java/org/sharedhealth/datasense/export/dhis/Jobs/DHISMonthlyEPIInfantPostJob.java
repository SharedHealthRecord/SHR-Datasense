package org.sharedhealth.datasense.export.dhis.Jobs;

import org.sharedhealth.datasense.export.dhis.reports.DHISMonthlyEPIInfantReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DHISMonthlyEPIInfantPostJob extends MonthlyJob {

    private static final Logger logger = LoggerFactory.getLogger(DHISMonthlyEPIInfantPostJob.class);
    private DHISMonthlyEPIInfantReport dhisMonthlyEPIInfantReport;

    public void setDhisMonthlyEPIInfantReport(DHISMonthlyEPIInfantReport dhisMonthlyEPIInfantReport) {
        this.dhisMonthlyEPIInfantReport = dhisMonthlyEPIInfantReport;
    }

    @Override
    protected void process(Map<String, Object> dataMap) {
        logger.info("Processing EPIInfantPostJob...");
        dhisMonthlyEPIInfantReport.process(dataMap);
    }
}