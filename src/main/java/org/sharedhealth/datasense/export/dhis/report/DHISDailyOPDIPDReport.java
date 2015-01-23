package org.sharedhealth.datasense.export.dhis.report;

import aggregatequeryservice.postservice;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.repository.FacilityDao;
import org.sharedhealth.datasense.util.DHISHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.sharedhealth.datasense.FacilityType.UPAZILA_HEALTH_COMPLEX_FACILITY_TYPE;

@Component
public class DHISDailyOPDIPDReport implements DHISReport {
    private FacilityDao facilityDao;
    private DataSource dataSource;
    private DHISHeaders dhisHeaders;
    private DatasenseProperties datasenseProperties;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DHISDailyOPDIPDReport.class);

    @Autowired
    public DHISDailyOPDIPDReport(FacilityDao facilityDao, DataSource dataSource, DHISHeaders dhisHeaders,
                                 DatasenseProperties datasenseProperties) {
        this.facilityDao = facilityDao;
        this.dataSource = dataSource;
        this.dhisHeaders = dhisHeaders;
        this.datasenseProperties = datasenseProperties;
    }

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

        HashMap<String, String> postHeaders = dhisHeaders.get();

        String pathToConfig = datasenseProperties.getDhisAqsConfigPath() + "daily_opd_ipd_report.json";

        logger.info(format("Posting Daily OPD IPD Emergency report for facility [%s] for date [%s]", facility
                .getFacilityName(), reportingDate));

        postservice.executeQueriesAndPostResultsSync(pathToConfig, dataSource, queryParams, extraParams, postHeaders);
    }
}
