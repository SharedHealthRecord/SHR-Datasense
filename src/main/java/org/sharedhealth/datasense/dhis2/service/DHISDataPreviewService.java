package org.sharedhealth.datasense.dhis2.service;

import org.apache.commons.lang3.StringUtils;
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
    @Autowired
    private DHISConfigDao dhisConfigDao;

    @Autowired
    private DHISDynamicReport dhisDynamicReport;

    public List<Map> fetchResults(ReportScheduleRequest scheduleRequest) {
        List<Map> arrayList = new ArrayList<Map>();
        for (String facilityId : scheduleRequest.getSelectedFacilities()) {
            DHISOrgUnitConfig orgUnitConfig = dhisConfigDao.findOrgUnitConfigFor(facilityId);
            DHISReportConfig configForDataset = dhisConfigDao.getReportConfig(scheduleRequest.getConfigId());
            HashMap<String, String> dataMap = new HashMap<>();
            createJobMap(dataMap, scheduleRequest, scheduleRequest.getDatasetId(), configForDataset, facilityId, orgUnitConfig);
            Map<String, Object> results = dhisDynamicReport.process(dataMap);
            Map<String, Object> trimmedResults = trimKeys(results);

            Map<String, Object> map = new HashMap<>();
            map.put("facilityName", orgUnitConfig.getFacilityName());
            map.put("facilityId", orgUnitConfig.getFacilityId());
            map.put("results", trimmedResults.entrySet());
            arrayList.add(map);
        }
        return arrayList;
    }


    private Map<String, Object> trimKeys(Map<String, Object> results) {
        HashMap<String, Object> map = new HashMap<>();
        for (String key : results.keySet()) {
            String trimmedKey = StringUtils.replaceChars(key, "_", " ");
            map.put(trimmedKey, results.get(key));
        }
        return map;
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
