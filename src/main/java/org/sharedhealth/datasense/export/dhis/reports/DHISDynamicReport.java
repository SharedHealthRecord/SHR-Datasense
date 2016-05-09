package org.sharedhealth.datasense.export.dhis.reports;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.sharedhealth.datasense.aqs.AqsFTLProcessor;
import org.sharedhealth.datasense.client.DHIS2Client;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.dhis2.model.DHISResponse;
import org.sharedhealth.datasense.model.Parameter;
import org.sharedhealth.datasense.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class DHISDynamicReport {

    private static final Logger logger = Logger.getLogger(DHISDynamicReport.class);

    final DataSource dataSource;
    final DatasenseProperties datasenseProperties;
    final AqsFTLProcessor aqsFTLProcessor;
    final DHIS2Client dhis2Client;
    private ConfigurationService configurationService;

    @Autowired
    public DHISDynamicReport(DataSource dataSource, DatasenseProperties datasenseProperties,
                             AqsFTLProcessor aqsFTLProcessor, DHIS2Client dhis2Client,
                             ConfigurationService configurationService) {
        this.dataSource = dataSource;
        this.datasenseProperties = datasenseProperties;
        this.aqsFTLProcessor = aqsFTLProcessor;
        this.dhis2Client = dhis2Client;
        this.configurationService = configurationService;
    }

    public void processAndPost(JobDataMap mergedJobDataMap) {

        String reportingStartDate = (String) mergedJobDataMap.get("paramStartDate");
        String reportingEndDate = (String) mergedJobDataMap.get("paramEndDate");
        String reportingPeriod = (String) mergedJobDataMap.get("paramReportingPeriod");

        String facilityId = (String) mergedJobDataMap.get("paramFacilityId");
        String orgUnitId = (String) mergedJobDataMap.get("paramOrgUnitId");
        String datasetId = (String) mergedJobDataMap.get("paramDatasetId");

        String configFile = (String) mergedJobDataMap.get("paramConfigFile");
        String periodType = (String) mergedJobDataMap.get("paramPeriodType");

        HashMap<String, String> queryParams = getQueryParams(reportingStartDate, reportingEndDate, facilityId, orgUnitId, datasetId, periodType);
        HashMap<String, String> extraParams = getExtraParams(reportingPeriod, orgUnitId, datasetId);

        logger.info(String.format("Posting data for facility [%s], dataset [%s] for date [%s]", facilityId, datasetId, reportingStartDate));
        try {
            postToDHIS2(configFile, queryParams, extraParams);
            logger.info(String.format("Done submitting data for facility [%s], dataset [%s] for date [%s]", facilityId, datasetId,
                    reportingStartDate));
        } catch (Exception e) {
            logger.error(String.format("Error submitting data for facility [%s], dataset [%s] for date [%s]",
                    facilityId, datasetId, reportingStartDate), e);
        }
    }

    public String process(Map<String, String> dataMap) {
        String reportingStartDate = dataMap.get("paramStartDate");
        String reportingEndDate = dataMap.get("paramEndDate");
        String reportingPeriod = dataMap.get("paramReportingPeriod");

        String facilityId = dataMap.get("paramFacilityId");
        String orgUnitId = dataMap.get("paramOrgUnitId");
        String datasetId = dataMap.get("paramDatasetId");

        String configFile = dataMap.get("paramConfigFile");

        HashMap<String, String> queryParams = getQueryParams(reportingStartDate, reportingEndDate, facilityId, orgUnitId, datasetId, dataMap.get("paramPeriodType"));
        HashMap<String, String> extraParams = getExtraParams(reportingPeriod, orgUnitId, datasetId);

        return previewQueryResult(configFile, queryParams, extraParams);
    }

    private HashMap<String, String> getExtraParams(String reportingPeriod, String orgUnitId, String datasetId) {
        HashMap<String, String> extraParams = new HashMap<>();
        extraParams.put("paramReportingPeriod", reportingPeriod);
        extraParams.put("paramDatasetId", datasetId);
        extraParams.put("paramOrgUnitId", orgUnitId);
        return extraParams;
    }

    private HashMap<String, String> getQueryParams(String reportingStartDate, String reportingEndDate, String facilityId, String orgUnitId, String datasetId, String paramPeriodType) {
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("paramStartDate", reportingStartDate);
        queryParams.put("paramEndDate", reportingEndDate);
        queryParams.put("paramPeriodType", paramPeriodType);
        queryParams.put("paramFacilityId", facilityId);
        queryParams.put("paramDatasetId", datasetId);
        queryParams.put("paramOrgUnitId", orgUnitId);

        List<Parameter> parameters = configurationService.allParameters();
        for (Parameter parameter : parameters) {
            queryParams.put(parameter.getParamName(), parameter.getParamValue());
        }
        return queryParams;
    }

    private void postToDHIS2(String configFile, HashMap<String, String> queryParams, HashMap<String, String> extraParams) {
        new JProcessor(configFile, queryParams, extraParams).invokeAndPostToDhis();
    }

    private String previewQueryResult(String configFile, HashMap<String, String> queryParams, HashMap<String, String> extraParams) {
        return new JProcessor(configFile, queryParams, extraParams).invoke();
    }

    private void logWhenErroredOut(String status, String dhisResponse) {
        if ((status != null) && (status.equalsIgnoreCase("200") || status.equalsIgnoreCase("201"))) {
            if (!StringUtils.isBlank(dhisResponse)) {
                if (dhisResponse.toUpperCase().contains("ERROR")) {
                    logger.error(String.format("DHIS Submission failed. Status %s. Response:%s", status, dhisResponse));
                }
            }
        } else {
            logger.error(String.format("DHIS Submission failed. Status %s. Response:%s", status, dhisResponse));
        }

    }

    private class JProcessor {
        private String configFile;
        private HashMap<String, String> queryParams;
        private HashMap<String, String> extraParams;

        public JProcessor(String configFile, HashMap<String, String> queryParams, HashMap<String, String> extraParams) {
            this.configFile = configFile;
            this.queryParams = queryParams;
            this.extraParams = extraParams;
        }

        public void invokeAndPostToDhis() {
            postUsingFTLProcessor(configFile, queryParams, extraParams);
        }

        public String invoke() {
            return getResultFromAqs(configFile, queryParams, extraParams);
        }

        private void postUsingFTLProcessor(String configFile, HashMap<String, String> queryParams, HashMap<String, String> extraParams) {
            HashMap<String, Object> params = getContent(queryParams, extraParams);
            try {
                String content = aqsFTLProcessor.process(configFile, params);
                logger.debug(String.format("Posting contents to DHIS2:- %s", content));
                DHISResponse dhisResponse = dhis2Client.post(content);
                logger.debug(dhisResponse.getValue());
            } catch (Exception e) {
                logger.error("Error occurred while posting data to DHIS2", e);
            }
        }

        private String getResultFromAqs(String configFile, HashMap<String, String> queryParams, HashMap<String, String> extraParams) {
            Map<String, Object> params = getContent(queryParams, extraParams);
            return aqsFTLProcessor.process(configFile, params);
        }

        private HashMap<String, Object> getContent(HashMap<String, String> queryParams, HashMap<String, String> extraParams) {
            HashMap<String, Object> params = new HashMap<>();
            params.putAll(queryParams);
            params.putAll(extraParams);
            return params;
        }
    }

}
