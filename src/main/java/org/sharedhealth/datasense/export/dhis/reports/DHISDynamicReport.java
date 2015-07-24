package org.sharedhealth.datasense.export.dhis.reports;

import aggregatequeryservice.postservice;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.LazySeq;
import clojure.lang.PersistentArrayMap;
import clojure.lang.Symbol;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.sharedhealth.datasense.aqs.AqsFTLProcessor;
import org.sharedhealth.datasense.client.DHIS2Client;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.dhis2.model.DHISResponse;
import org.sharedhealth.datasense.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;

import static org.sharedhealth.datasense.util.HeaderUtil.getDhisHeaders;

@Component
public class DHISDynamicReport {

    private static final Logger logger = Logger.getLogger(DHISDynamicReport.class);

    final DataSource dataSource;
    final DatasenseProperties datasenseProperties;
    final AqsFTLProcessor aqsFTLProcessor;
    final DHIS2Client dhis2Client;

    @Autowired
    public DHISDynamicReport(DataSource dataSource, DatasenseProperties datasenseProperties, AqsFTLProcessor aqsFTLProcessor, DHIS2Client dhis2Client) {
        this.dataSource = dataSource;
        this.datasenseProperties = datasenseProperties;
        this.aqsFTLProcessor = aqsFTLProcessor;
        this.dhis2Client = dhis2Client;
    }

    public void process(JobDataMap mergedJobDataMap) {

        HashMap<String, String> queryParams = new HashMap<>();
        String reportingStartDate = (String) mergedJobDataMap.get("paramStartDate");
        String reportingEndDate = (String) mergedJobDataMap.get("paramEndDate");
        String reportingPeriod = (String) mergedJobDataMap.get("paramReportingPeriod");

        String facilityId = (String) mergedJobDataMap.get("paramFacilityId");
        String orgUnitId = (String) mergedJobDataMap.get("paramOrgUnitId");
        String datasetId = (String) mergedJobDataMap.get("paramDatasetId");
        String pncGivenWithin48HoursUUID = (String) mergedJobDataMap.get("pncGivenWithin48HoursUUID");
        String newBornCareUUID = (String) mergedJobDataMap.get("newBornCare");
        String pentaThreeDrugUuid = (String) mergedJobDataMap.get("pentaThreeDrugUuid");

        queryParams.put("paramStartDate", reportingStartDate);
        queryParams.put("paramEndDate", reportingEndDate);
        queryParams.put("paramPeriodType", (String) mergedJobDataMap.get("paramPeriodType"));
        queryParams.put("paramFacilityId", facilityId);
        queryParams.put("paramDatasetId", datasetId);
        queryParams.put("paramOrgUnitId", orgUnitId);
        queryParams.put("pncGivenWithin48HoursUUID", pncGivenWithin48HoursUUID);
        queryParams.put("newBornCare", newBornCareUUID);
        queryParams.put("pentaThreeDrugUuid", pentaThreeDrugUuid);

        String configFile = (String) mergedJobDataMap.get("paramConfigFile");

        HashMap<String, String> extraParams = new HashMap<>();
        extraParams.put("paramReportingPeriod", reportingPeriod);
        extraParams.put("paramDatasetId", datasetId);
        extraParams.put("paramOrgUnitId", orgUnitId);

        logger.info(String.format("Posting data for facility [%s], dataset [%s] for date [%s]", facilityId, datasetId, reportingStartDate));
        try {
            postToDHIS2(configFile, queryParams, extraParams);
            logger.info(String.format("Done submitting data for facility [%s], dataset [%s] for date [%s]", facilityId, datasetId,
                    reportingStartDate));
        } catch (Exception e) {
            System.out.println(e);
            logger.error(String.format("Error submitting data for facility [%s], dataset [%s] for date [%s]",
                            facilityId, datasetId, reportingStartDate), e);
        }
    }

    private void postToDHIS2(String configFile, HashMap<String, String> queryParams, HashMap<String, String> extraParams) {
        new JProcessor(configFile, queryParams, extraParams).invoke();
        //new CljProcessor(configFile, queryParams, extraParams).invoke();
    }

    private String getValueFromMap(IPersistentMap map,String key){
        Object value=map.valAt(Keyword.intern(Symbol.create(key)));
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    private void logWhenErroredOut(String status, String dhisResponse) {
        if ((status != null) && (status.equalsIgnoreCase("200") || status.equalsIgnoreCase("201")) ) {
            if (!StringUtils.isBlank(dhisResponse)) {
                if (dhisResponse.toUpperCase().contains("ERROR")) {
                    logger.error(String.format("DHIS Submission failed. Status %s. Response:%s", status, dhisResponse));
                }
            }
        }
        else {
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

        public void invoke() {
            postUsingFTLProcessor(configFile, queryParams, extraParams);
        }

        private void postUsingFTLProcessor(String configFile, HashMap<String, String> queryParams, HashMap<String, String> extraParams) {
            HashMap<String, Object> params = new HashMap<>();
            params.putAll(queryParams);
            params.putAll(extraParams);
            String content = aqsFTLProcessor.process(configFile, params);
            try {
                DHISResponse dhisResponse = dhis2Client.post(content);
                logger.debug(dhisResponse.getValue());
            } catch (Exception e) {
                logger.error("Error occurred while posting data to DHIS2", e);
            }
        }
    }

    private class CljProcessor {
        private String configFile;
        private HashMap<String, String> queryParams;
        private HashMap<String, String> extraParams;

        public CljProcessor(String configFile, HashMap<String, String> queryParams, HashMap<String, String> extraParams) {
            this.configFile = configFile;
            this.queryParams = queryParams;
            this.extraParams = extraParams;
        }

        public void invoke() {
            postUsingAqsClj(configFile, queryParams, extraParams);
        }

        private void postUsingAqsClj(String configFile, HashMap<String, String> queryParams, HashMap<String, String> extraParams) {
            String configFilePath = StringUtil.ensureSuffix(datasenseProperties.getAqsConfigLocationPath(), "/") + configFile;
            HashMap<String, String> postHeaders = getDhisHeaders(datasenseProperties);
            Object result = postservice.executeQueriesAndPostResultsSync(configFilePath, dataSource, queryParams, extraParams,
                    postHeaders, datasenseProperties.getDhisDataValueSetsUrl());
            if (!(result instanceof LazySeq)) {
                logger.debug("result:" + result.toString());
            } else {
                LazySeq seq = (LazySeq) result;
                if (!seq.isEmpty()) {
                    Object response = seq.get(0);
                    if (response instanceof PersistentArrayMap) {
                        PersistentArrayMap responseMap = (PersistentArrayMap) response;
                        String status = getValueFromMap(responseMap, "status");
                        String dhisResponse = getValueFromMap(responseMap, "response");
                        logWhenErroredOut(status, dhisResponse);
                    } else {
                        logger.debug("result:" + response.toString());
                    }
                }
            }
        }
    }
}
