package org.sharedhealth.datasense.export.dhis.report;

import aggregatequeryservice.postservice;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.repository.FacilityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.sharedhealth.datasense.util.HttpUtil.getBase64Authentication;

@Component
public class DHISDailyOPDIPDReport implements DHISReport {
    private FacilityDao facilityDao;
    private DatasenseProperties datasenseProperties;
    private static final String facilityType = "Upazila Health Complex";
    private DataSource dataSource;


    @Autowired
    public DHISDailyOPDIPDReport(FacilityDao facilityDao, DatasenseProperties properties, DataSource dataSource) {
        this.facilityDao = facilityDao;
        this.datasenseProperties = properties;
        this.dataSource = dataSource;
    }

    @Override
    public void process(Map<String, Object> dataMap) {
        String reportingDate = (String) dataMap.get("reportingDate");
        List<Facility> facilitiesByType = facilityDao.findFacilitiesByType(facilityType);
        for (Facility facility : facilitiesByType) {
            if (facility.getDhisOrgUnitUid() != null)
                processReportForFacility(facility, reportingDate);
        }
    }

    private void processReportForFacility(Facility facility, String reportingDate) {
        String period = reportingDate.replaceAll("-", "");
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("FACILITY", facility.getFacilityId());
        queryParams.put("ENC_DATE", reportingDate);

        HashMap<String, String> extraParams = new HashMap<>();
        extraParams.put("period", period);
        extraParams.put("orgUnit", facility.getDhisOrgUnitUid());

        HashMap<String, String> postHeaders = new HashMap<>();
        String authentication = getBase64Authentication(datasenseProperties.getDhisUserName(), datasenseProperties.getDhisPassword());
        postHeaders.put("Authorization", authentication);
        postHeaders.put("Content-Type", "application/json");

        String pathToConfig = "/opt/datasense/lib/dhis_config/aqs_config/daily_opd_ipd_report.json";

        postservice.executeQueriesAndPostResultsSync(pathToConfig, dataSource, queryParams, extraParams, postHeaders);
    }
}
