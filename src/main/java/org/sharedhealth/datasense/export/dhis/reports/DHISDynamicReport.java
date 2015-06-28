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
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;

import static org.sharedhealth.datasense.util.HeaderUtil.getDhisHeaders;

@Component
public class DHISDynamicReport {

    private static final Logger logger = Logger.getLogger(DHISDynamicReport.class);

    @Autowired
    DataSource dataSource;

    @Autowired
    DatasenseProperties datasenseProperties;

    public void process(JobDataMap mergedJobDataMap) {

        HashMap<String, String> queryParams = new HashMap<>();
        String reportingStartDate = (String) mergedJobDataMap.get("paramStartDate");
        String reportingEndDate = (String) mergedJobDataMap.get("paramEndDate");
        String reportingPeriod = (String) mergedJobDataMap.get("paramReportingPeriod");

        String facilityId = (String) mergedJobDataMap.get("paramFacilityId");
        String orgUnitId = (String) mergedJobDataMap.get("paramOrgUnitId");
        String datasetId = (String) mergedJobDataMap.get("paramDatasetId");

        queryParams.put("paramStartDate", reportingStartDate);
        queryParams.put("paramEndDate", reportingEndDate);
        queryParams.put("paramPeriodType", (String) mergedJobDataMap.get("paramPeriodType"));
        queryParams.put("paramFacilityId", facilityId);
        queryParams.put("paramDatasetId", datasetId);
        queryParams.put("paramOrgUnitId", orgUnitId);

        String configFile = (String) mergedJobDataMap.get("paramConfigFile");

        String configFilePath = StringUtil.ensureSuffix(datasenseProperties.getDhisAqsConfigPath(), "/") + configFile;

        HashMap<String, String> extraParams = new HashMap<>();
        extraParams.put("paramReportingPeriod", reportingPeriod);
        extraParams.put("paramDatasetId", datasetId);
        extraParams.put("paramOrgUnitId", orgUnitId);

        HashMap<String, String> postHeaders = getDhisHeaders(datasenseProperties);


        logger.info(String.format("Posting data for facility [%s], dataset [%s] for date [%s]", facilityId, datasetId, reportingStartDate));
        try {
            Object result = postservice.executeQueriesAndPostResultsSync(configFilePath, dataSource, queryParams, extraParams,
                    postHeaders, datasenseProperties.getDhisDataValueSetsUrl());
            logger.info(String.format("Done submitting data for facility [%s], dataset [%s] for date [%s]", facilityId, datasetId,
                    reportingStartDate));
            if (result instanceof clojure.lang.LazySeq) {
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
            } else {
                logger.debug("result:" + result.toString());
            }
        } catch (Exception e) {
            System.out.println(e);
            logger.error(String.format("Error submitting data for facility [%s], dataset [%s] for date [%s]",
                            facilityId, datasetId, reportingStartDate), e);
        }
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

}
