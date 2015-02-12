package org.sharedhealth.datasense.export.dhis.reports;

import org.springframework.stereotype.Component;

@Component
public class DHISMonthlyEPIInfantReport extends MonthlyDHISReport {
    @Override
    public String getConfigFilepath() {
        return "monthly_epi_infant_report.json";
    }
}