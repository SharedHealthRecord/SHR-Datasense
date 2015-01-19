package org.sharedhealth.datasense.export.dhis.report;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.sharedhealth.datasense.FacilityType.UPAZILA_HEALTH_COMPLEX_FACILITY_TYPE;
import static org.sharedhealth.datasense.FacilityType.UPAZILA_LEVEL_OFFICE_FACILITY_TYPE;

public class DHISMonthlyEPIInfantReport implements DHISReport{
    @Override
    public void process(Map<String, Object> dataMap) {
        String reportingMonth = (String) dataMap.get("reportingMonth");
        List<String> facilityTypes = Arrays.asList(UPAZILA_HEALTH_COMPLEX_FACILITY_TYPE, UPAZILA_LEVEL_OFFICE_FACILITY_TYPE);
        for (String facilityType : facilityTypes) {

        }
    }
}
