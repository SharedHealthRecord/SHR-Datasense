package org.sharedhealth.datasense.export.dhis.reports;

import org.springframework.stereotype.Component;

@Component
public class DHISMonthlyColposcopyReport extends MonthlyDHISReport {
    @Override
    public String getConfigFilepath() {
        return "monthly_colposcopy_report.json";
    }
}