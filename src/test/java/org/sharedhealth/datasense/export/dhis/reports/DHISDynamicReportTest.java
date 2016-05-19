package org.sharedhealth.datasense.export.dhis.reports;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.quartz.JobDataMap;
import org.sharedhealth.datasense.aqs.AqsFTLProcessor;
import org.sharedhealth.datasense.client.DHIS2Client;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.service.ConfigurationService;

import javax.sql.DataSource;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class DHISDynamicReportTest {
    @Mock
    private DataSource dataSource;
    @Mock
    private DatasenseProperties datasenseProperties;
    @Mock
    private AqsFTLProcessor aqsFTLProcessor;
    @Mock
    private DHIS2Client dhis2Client;
    @Mock
    private ConfigurationService configurationService;


    private DHISDynamicReport dhisDynamicReport;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        dhisDynamicReport = new DHISDynamicReport(dataSource, datasenseProperties, aqsFTLProcessor, dhis2Client, configurationService);
    }

    @Test
    public void shouldInvokeAqsFTLProcessorWithGivenParams() throws Exception {
        String configFile = "some.json";
        String period = "20161010";
        String startAndEndDates = "10/10/2016";

        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("paramStartDate", startAndEndDates);
        jobDataMap.put("paramEndDate", startAndEndDates);
        jobDataMap.put("paramPeriodType", "Daily");
        jobDataMap.put("paramFacilityId", "12345");
        jobDataMap.put("paramDatasetId", "abc123");
        jobDataMap.put("paramOrgUnitId", "10001");
        jobDataMap.put("paramConfigFile", configFile);
        jobDataMap.put("paramReportingPeriod", period);
        jobDataMap.put("paramScheduleType", "once");
        jobDataMap.put("paramPreviousPeriods", null);

        dhisDynamicReport.processAndPost(jobDataMap);

        ArgumentCaptor<Map> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(aqsFTLProcessor).process(eq(configFile), paramsCaptor.capture());

        Map paramsCaptorValue = paramsCaptor.getValue();
        assertEquals(startAndEndDates, paramsCaptorValue.get("paramStartDate"));
        assertEquals(startAndEndDates, paramsCaptorValue.get("paramEndDate"));
        assertEquals(period, paramsCaptorValue.get("paramReportingPeriod"));
        assertNull(paramsCaptorValue.get("paramScheduleType"));
        assertNull(paramsCaptorValue.get("paramPreviousPeriods"));
    }

    @Test
    public void shouldPostProcessedFTLToDHIS() throws Exception {
        JobDataMap jobDataMap = new JobDataMap();

        String content = "some content";
        when(aqsFTLProcessor.process(anyString(), anyMap())).thenReturn(content);

        dhisDynamicReport.processAndPost(jobDataMap);

        verify(dhis2Client, times(1)).post(content);
    }

    @Test
    public void shouldCalculateReportingPeriodAndStartEndDatesForDailyRecurringReports() throws Exception {
        String configFile = "some.json";
        String period = "20161010";
        String startAndEndDates = "10/10/2016";

        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("paramStartDate", startAndEndDates);
        jobDataMap.put("paramEndDate", startAndEndDates);
        jobDataMap.put("paramPeriodType", "Daily");
        jobDataMap.put("paramFacilityId", "12345");
        jobDataMap.put("paramDatasetId", "abc123");
        jobDataMap.put("paramOrgUnitId", "10001");
        jobDataMap.put("paramConfigFile", configFile);
        jobDataMap.put("paramReportingPeriod", period);
        jobDataMap.put("paramScheduleType", "repeat");
        jobDataMap.put("paramPreviousPeriods", 3);

        dhisDynamicReport.processAndPost(jobDataMap);

        ArgumentCaptor<Map> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(aqsFTLProcessor).process(eq(configFile), paramsCaptor.capture());

        Map paramsCaptorValue = paramsCaptor.getValue();
        assertEquals("07/10/2016", paramsCaptorValue.get("paramStartDate"));
        assertEquals("07/10/2016", paramsCaptorValue.get("paramEndDate"));
        assertEquals("20161007", paramsCaptorValue.get("paramReportingPeriod"));
        assertNull(paramsCaptorValue.get("paramScheduleType"));
        assertNull(paramsCaptorValue.get("paramPreviousPeriods"));
    }
}