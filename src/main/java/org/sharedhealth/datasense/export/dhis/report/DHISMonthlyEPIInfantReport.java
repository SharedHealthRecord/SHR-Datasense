package org.sharedhealth.datasense.export.dhis.report;

import aggregatequeryservice.postservice;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.repository.FacilityDao;
import org.sharedhealth.datasense.util.DHISHeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.sharedhealth.datasense.FacilityType.UPAZILA_HEALTH_COMPLEX_FACILITY_TYPE;
import static org.sharedhealth.datasense.FacilityType.UPAZILA_LEVEL_OFFICE_FACILITY_TYPE;

@Component
public class DHISMonthlyEPIInfantReport implements DHISReport {

    private FacilityDao facilityDao;
    private DHISHeaderUtil dhisHeaderUtil;
    private DatasenseProperties datasenseProperties;
    private DataSource dataSource;

    private static final Logger logger = LoggerFactory.getLogger(DHISMonthlyEPIInfantReport.class);

    @Autowired
    public DHISMonthlyEPIInfantReport(FacilityDao facilityDao, DataSource dataSource, DHISHeaderUtil dhisHeaderUtil, DatasenseProperties datasenseProperties) {
        this.facilityDao = facilityDao;
        this.dataSource = dataSource;
        this.dhisHeaderUtil = dhisHeaderUtil;
        this.datasenseProperties = datasenseProperties;
    }

    @Override
    public void process(Map<String, Object> dataMap) {
        String reportingMonth = (String) dataMap.get("reportingMonth");
        List<String> facilityTypes = asList(UPAZILA_HEALTH_COMPLEX_FACILITY_TYPE, UPAZILA_LEVEL_OFFICE_FACILITY_TYPE);
        List<Facility> facilities = facilityDao.findFacilitiesByTypes(facilityTypes);
        for (Facility facility : facilities) {
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

        HashMap<String, String> postHeaders = dhisHeaderUtil.getDhisHeaders();

        String pathToConfig = datasenseProperties.getDhisAqsConfigPath() + "monthly_epi_infant_report.json";

        logger.info(format("Posting EPI Infant report for facility [%s] for month [%s]", facility.getFacilityName(), reportingMonth));
        postservice.executeQueriesAndPostResultsSync(pathToConfig, dataSource, queryParams, extraParams, postHeaders);
    }
}