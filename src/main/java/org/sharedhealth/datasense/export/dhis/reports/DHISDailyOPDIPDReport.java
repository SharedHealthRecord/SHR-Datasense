package org.sharedhealth.datasense.export.dhis.reports;

import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Component
public class DHISDailyOPDIPDReport extends DailyDHISReport {
    @Override
    public String getConfigFilepath() {
        return "daily_opd_ipd_report.json";
    }
}
