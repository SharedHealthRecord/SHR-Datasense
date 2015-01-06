package org.sharedhealth.datasense.export.dhis.report;

import aggregatequeryservice.postservice;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.export.dhis.annotations.DhisParam;
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.repository.FacilityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.util.*;

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
        String authentication = "Basic " + getBase64Authentication();
        postHeaders.put("Authorization", authentication);
        postHeaders.put("Content-Type", "application/json");

        String pathToConfig = "dhis/aqs_config/daily_opd_ipd_report.json";

        postservice.executeQueriesAndPostResultsSync(pathToConfig, dataSource, queryParams, extraParams, postHeaders);
    }

    private String getBase64Authentication() {
        String credentials = datasenseProperties.getDhisUserName() + ":" + datasenseProperties.getDhisPassword();
        byte[] credentialBytes = Base64.encodeBase64(credentials.getBytes());
        return new String(credentialBytes);
    }


}
