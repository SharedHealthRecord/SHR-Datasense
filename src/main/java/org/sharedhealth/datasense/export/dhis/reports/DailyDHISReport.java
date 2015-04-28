package org.sharedhealth.datasense.export.dhis.reports;

import aggregatequeryservice.postservice;
import org.sharedhealth.datasense.model.Facility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.sharedhealth.datasense.FacilityType.UPAZILA_HEALTH_COMPLEX_FACILITY_TYPE;
import static org.sharedhealth.datasense.util.HeaderUtil.getDhisHeaders;

public abstract class DailyDHISReport extends DHISReport {


    @Override
    public void process(Map<String, Object> dataMap) {
        String reportingDate = (String) dataMap.get("reportingDate");
        List<Facility> facilitiesByType = facilityDao.findFacilitiesByTypes(asList
                (UPAZILA_HEALTH_COMPLEX_FACILITY_TYPE));
        for (Facility facility : facilitiesByType) {
            if (facility.getDhisOrgUnitUid() != null)
                postReportForFacility(facility, reportingDate);
        }
    }

    private void postReportForFacility(Facility facility, String reportingDate) {
        String period = reportingDate.replaceAll("-", "");
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("FACILITY", facility.getFacilityId());
        queryParams.put("ENC_DATE", reportingDate);

        HashMap<String, String> extraParams = new HashMap<>();
        extraParams.put("period", period);
        extraParams.put("orgUnit", facility.getDhisOrgUnitUid());

        HashMap<String, String> postHeaders = getDhisHeaders(datasenseProperties);

        String pathToConfig = datasenseProperties.getDhisAqsConfigPath() + getConfigFilepath();

        logger.debug(format("Posting Daily OPD IPD Emergency report for facility [%s] for date [%s]", facility
                .getFacilityName(), reportingDate));

        postservice.executeQueriesAndPostResultsSync(pathToConfig, dataSource, queryParams, extraParams, postHeaders);
    }

    public abstract String getConfigFilepath();
}
