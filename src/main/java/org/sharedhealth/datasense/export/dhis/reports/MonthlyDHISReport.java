package org.sharedhealth.datasense.export.dhis.reports;

import aggregatequeryservice.postservice;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.util.HeaderUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.sharedhealth.datasense.FacilityType.*;
import static org.sharedhealth.datasense.util.HeaderUtil.getDhisHeaders;

public abstract class MonthlyDHISReport extends DHISReport {

    @Override
    public void process(Map<String, Object> dataMap) {

        String reportingMonth = (String) dataMap.get("reportingMonth");
        List<String> facilityTypes = asList(MEDICAL_UNIVERSITY_FACILITY_TYPE, UPAZILA_HEALTH_COMPLEX_FACILITY_TYPE, UPAZILA_LEVEL_OFFICE_FACILITY_TYPE);
        List<Facility> facilities = facilityDao.findFacilitiesByTypes(facilityTypes);
        for (Facility facility : facilities) {
            if (facility.getDhisOrgUnitUid() != null)
                postReportForFacility(facility, reportingMonth);
        }
    }

    private void postReportForFacility(Facility facility, String reportingMonth) {
        String period = reportingMonth.replaceAll("-", "");
        String year = StringUtils.substringBefore(reportingMonth, "-");
        String month = StringUtils.substringAfter(reportingMonth, "-");

        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("FACILITY", facility.getFacilityId());
        queryParams.put("YEAR", year);
        queryParams.put("MONTH", month);

        HashMap<String, String> extraParams = new HashMap<>();
        extraParams.put("period", period);
        extraParams.put("orgUnit", facility.getDhisOrgUnitUid());

        HashMap<String, String> postHeaders = getDhisHeaders(datasenseProperties);

        String pathToConfig = datasenseProperties.getDhisAqsConfigPath() + getConfigFilepath();

        logger.info(format("Posting for facility [%s] for month [%s]", facility.getFacilityName(),
                reportingMonth));
        postservice.executeQueriesAndPostResultsSync(pathToConfig, dataSource, queryParams, extraParams, postHeaders);
    }

    public abstract String getConfigFilepath();
}
