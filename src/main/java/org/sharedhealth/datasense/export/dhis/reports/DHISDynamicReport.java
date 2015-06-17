package org.sharedhealth.datasense.export.dhis.reports;

import aggregatequeryservice.postservice;
import clojure.lang.LazySeq;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        queryParams.put("paramEndDate",   reportingEndDate);
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
            Object result = postservice.executeQueriesAndPostResultsSync(configFilePath, dataSource, queryParams, extraParams, postHeaders);
            logger.info(String.format("Done submitting data for facility [%s], dataset [%s] for date [%s]", facilityId, datasetId, reportingStartDate));
            if (result instanceof clojure.lang.LazySeq) {
                LazySeq seq = (LazySeq) result;
                if (!seq.isEmpty()) {
                    logger.debug("result:" + seq.get(0));
                }
            } else {
                logger.debug("result:" + result.toString());
            }
        } catch (Exception e) {
            logger.error(
                    String.format("Error submitting data for facility [%s], dataset [%s] for date [%s]", facilityId, datasetId, reportingStartDate),
                    e);
        }
    }

}
