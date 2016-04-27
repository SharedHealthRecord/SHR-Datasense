package org.sharedhealth.datasense.dhis2.service;

import org.sharedhealth.datasense.dhis2.controller.ReportScheduleRequest;
import org.sharedhealth.datasense.dhis2.model.DHISOrgUnitConfig;
import org.sharedhealth.datasense.dhis2.model.DHISReportConfig;
import org.sharedhealth.datasense.dhis2.repository.DHISConfigDao;
import org.sharedhealth.datasense.export.dhis.reports.DHISDynamicReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DHISDataPreviewService {
    @Autowired
    private DHISConfigDao dhisConfigDao;

    @Autowired
    private DHISDynamicReport dhisDynamicReport;

    public Map<String, Object> fetchResults(ReportScheduleRequest scheduleRequest) {
        String facilityId = scheduleRequest.getSelectedFacilities().get(0);
        DHISOrgUnitConfig orgUnitConfig = dhisConfigDao.findOrgUnitConfigFor(facilityId);
        DHISReportConfig configForDataset = dhisConfigDao.getReportConfig(scheduleRequest.getConfigId());
        HashMap<String, String> dataMap = new HashMap<>();
        createJobMap(dataMap, scheduleRequest, scheduleRequest.getDatasetId(), configForDataset, facilityId, orgUnitConfig);
        return dhisDynamicReport.process(dataMap);
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
