package org.sharedhealth.datasense.export.dhis.Jobs;

import org.sharedhealth.datasense.export.dhis.annotations.DhisParam;
import org.sharedhealth.datasense.export.dhis.reports.DHISDailyOPDIPDReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@DhisParam({"reportingDate"})
public class DHISDailyOPDIPDPostJob extends DailyJob {
    private DHISDailyOPDIPDReport dhisDailyOPDIPDReport;

    private static final Logger logger = LoggerFactory.getLogger(DHISDailyOPDIPDPostJob.class);


    public void setDhisDailyOPDIPDReport(DHISDailyOPDIPDReport dhisDailyOPDIPDReport) {
        this.dhisDailyOPDIPDReport = dhisDailyOPDIPDReport;
    }

    @Override
    protected void process(Map<String, Object> dataMap) {
        dhisDailyOPDIPDReport.process(dataMap);
    }
}
