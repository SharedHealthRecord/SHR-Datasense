package org.sharedhealth.datasense.dhis2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.sharedhealth.datasense.dhis2.controller.ReportScheduleRequest;
import org.sharedhealth.datasense.dhis2.model.DHISOrgUnitConfig;
import org.sharedhealth.datasense.dhis2.model.DHISReportConfig;
import org.sharedhealth.datasense.dhis2.repository.DHISConfigDao;
import org.sharedhealth.datasense.export.dhis.reports.DHISDynamicReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DHISDataPreviewService {
    private static final Logger logger = Logger.getLogger(DHISDataPreviewService.class);

    @Autowired
    private DHISConfigDao dhisConfigDao;

    @Autowired
    private DHISDynamicReport dhisDynamicReport;

    public List<Map> fetchResults(ReportScheduleRequest scheduleRequest, List<String> formErrors) {
        List<Map> arrayList = new ArrayList<>();
        for (String facilityId : scheduleRequest.getSelectedFacilities()) {
            DHISOrgUnitConfig orgUnitConfig = dhisConfigDao.findOrgUnitConfigFor(facilityId);
            if(orgUnitConfig == null) {
                formErrors.add(String.format("Facility with id [%s] is not configured with a valid DHIS Organization", facilityId));
                return null;
            }
            try {
                DHISReportConfig configForDataset = dhisConfigDao.getReportConfig(scheduleRequest.getConfigId());
                HashMap<String, String> dataMap = new HashMap<>();
                createJobMap(dataMap, scheduleRequest, scheduleRequest.getDatasetId(), configForDataset, facilityId, orgUnitConfig);
                String proccessedTmpl = dhisDynamicReport.process(dataMap);
                Map<String, Object> map = new HashMap<>();
                map.put("facilityName", orgUnitConfig.getFacilityName());
                map.put("facilityId", orgUnitConfig.getFacilityId());
                map.put("results", new ObjectMapper().readValue(proccessedTmpl, Map.class));
                arrayList.add(map);
            } catch (Exception e) {
                String errorMsg = String.format("Error processing data for facility [%s], dataset [%s] for period [%s]",
                        orgUnitConfig.getFacilityName(), scheduleRequest.getDatasetName(), scheduleRequest.reportPeriod().period());
                logger.error(errorMsg, e);
                formErrors.add(errorMsg);
                return null;
            }
        }
        return arrayList;
    }


    private void createJobMap(Map dataMap, ReportScheduleRequest scheduleRequest, String datasetId, DHISReportConfig configForDataset, String facilityId, DHISOrgUnitConfig orgUnitConfig) {
        dataMap.put("paramStartDate", scheduleRequest.reportPeriod().startDate());
        dataMap.put("paramEndDate", scheduleRequest.reportPeriod().endDate());
        dataMap.put("paramPeriodType", scheduleRequest.getPeriodType());
        dataMap.put("paramFacilityId", facilityId);
        dataMap.put("paramDatasetId", datasetId);
        dataMap.put("paramOrgUnitId", orgUnitConfig.getOrgUnitId());
        dataMap.put("paramConfigFile", configForDataset.getConfigFile());
        dataMap.put("paramReportingPeriod", scheduleRequest.reportPeriod().period());
    }
}
