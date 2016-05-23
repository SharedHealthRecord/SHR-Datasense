package org.sharedhealth.datasense.export.dhis.reports;

import org.apache.commons.lang3.time.DateUtils;
import org.h2.util.DateTimeUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.quartz.JobDataMap;
import org.sharedhealth.datasense.aqs.AqsFTLProcessor;
import org.sharedhealth.datasense.client.DHIS2Client;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.service.ConfigurationService;
import org.sharedhealth.datasense.util.DateUtil;

import javax.sql.DataSource;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
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
        String startAndEndDates = "2016-10-10";

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
        String startAndEndDates = "2016-10-10";

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
        jobDataMap.put("paramPreviousPeriods", "3");

        Date date = DateUtil.parseDate("2016-05-16");
        DateTime currentDateTime = new DateTime(date);
        org.joda.time.DateTimeUtils.setCurrentMillisFixed(currentDateTime.getMillis());

        dhisDynamicReport.processAndPost(jobDataMap);

        ArgumentCaptor<Map> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(aqsFTLProcessor).process(eq(configFile), paramsCaptor.capture());
        Map paramsCaptorValue = paramsCaptor.getValue();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, -3);
        String expected = DateUtil.toGivenFormatString(cal.getTime(), DateUtil.SIMPLE_DATE_FORMAT);
        assertEquals(expected, paramsCaptorValue.get("paramStartDate"));
        assertEquals(expected, paramsCaptorValue.get("paramEndDate"));
        assertEquals("20160513", paramsCaptorValue.get("paramReportingPeriod"));
        assertNull(paramsCaptorValue.get("paramScheduleType"));
        assertNull(paramsCaptorValue.get("paramPreviousPeriods"));
    }

    @Test
    public void shouldCalculateReportingPeriodAndStartEndDatesForQuarterlyRecurringReports() throws Exception {
        String configFile = "some.json";
        String period = "2016Q1";
        String startDate = "2016-01-01";
        String endDate = "2016-03-31";

        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("paramStartDate", startDate);
        jobDataMap.put("paramEndDate", endDate);
        jobDataMap.put("paramPeriodType", "Quarterly");
        jobDataMap.put("paramFacilityId", "12345");
        jobDataMap.put("paramDatasetId", "abc123");
        jobDataMap.put("paramOrgUnitId", "10001");
        jobDataMap.put("paramConfigFile", configFile);
        jobDataMap.put("paramReportingPeriod", period);
        jobDataMap.put("paramScheduleType", "repeat");
        jobDataMap.put("paramPreviousPeriods", "2");

        Date date = DateUtil.parseDate("2016-05-16");
        DateTime currentDateTime = new DateTime(date);
        org.joda.time.DateTimeUtils.setCurrentMillisFixed(currentDateTime.getMillis());

        dhisDynamicReport.processAndPost(jobDataMap);

        ArgumentCaptor<Map> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(aqsFTLProcessor).process(eq(configFile), paramsCaptor.capture());
        Map paramsCaptorValue = paramsCaptor.getValue();
        assertEquals("2015-10-01", paramsCaptorValue.get("paramStartDate"));
        assertEquals("2015-12-31", paramsCaptorValue.get("paramEndDate"));
        assertEquals("2015Q4", paramsCaptorValue.get("paramReportingPeriod"));
        assertNull(paramsCaptorValue.get("paramScheduleType"));
        assertNull(paramsCaptorValue.get("paramPreviousPeriods"));
    }
    @Test
    public void shouldCalculateReportingPeriodAndStartEndDatesForMonthlyRecurringReports() throws Exception {
        String configFile = "some.json";
        String period = "201601";
        String startDate = "2016-01-01";
        String endDate = "2016-01-31";

        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("paramStartDate", startDate);
        jobDataMap.put("paramEndDate", endDate);
        jobDataMap.put("paramPeriodType", "Monthly");
        jobDataMap.put("paramFacilityId", "12345");
        jobDataMap.put("paramDatasetId", "abc123");
        jobDataMap.put("paramOrgUnitId", "10001");
        jobDataMap.put("paramConfigFile", configFile);
        jobDataMap.put("paramReportingPeriod", period);
        jobDataMap.put("paramScheduleType", "repeat");
        jobDataMap.put("paramPreviousPeriods", "2");

        Date date = DateUtil.parseDate("2016-05-16");
        DateTime currentDateTime = new DateTime(date);
        org.joda.time.DateTimeUtils.setCurrentMillisFixed(currentDateTime.getMillis());

        dhisDynamicReport.processAndPost(jobDataMap);

        ArgumentCaptor<Map> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(aqsFTLProcessor).process(eq(configFile), paramsCaptor.capture());
        Map paramsCaptorValue = paramsCaptor.getValue();
        assertEquals("2016-03-01", paramsCaptorValue.get("paramStartDate"));
        assertEquals("2016-03-31", paramsCaptorValue.get("paramEndDate"));
        assertEquals("201603", paramsCaptorValue.get("paramReportingPeriod"));
        assertNull(paramsCaptorValue.get("paramScheduleType"));
        assertNull(paramsCaptorValue.get("paramPreviousPeriods"));
    }
    @Test
    public void shouldCalculateReportingPeriodAndStartEndDatesForWeeklyRecurringReports() throws Exception {
        String configFile = "some.json";
        String period = "2016w2";
        String startDate = "2016-01-03";
        String endDate = "2016-01-09";

        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("paramStartDate", startDate);
        jobDataMap.put("paramEndDate", endDate);
        jobDataMap.put("paramPeriodType", "Weekly");
        jobDataMap.put("paramFacilityId", "12345");
        jobDataMap.put("paramDatasetId", "abc123");
        jobDataMap.put("paramOrgUnitId", "10001");
        jobDataMap.put("paramConfigFile", configFile);
        jobDataMap.put("paramReportingPeriod", period);
        jobDataMap.put("paramScheduleType", "repeat");
        jobDataMap.put("paramPreviousPeriods", "2");

        Date date = DateUtil.parseDate("2016-05-16");
        DateTime currentDateTime = new DateTime(date);
        org.joda.time.DateTimeUtils.setCurrentMillisFixed(currentDateTime.getMillis());

        dhisDynamicReport.processAndPost(jobDataMap);

        ArgumentCaptor<Map> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(aqsFTLProcessor).process(eq(configFile), paramsCaptor.capture());
        Map paramsCaptorValue = paramsCaptor.getValue();
        assertEquals("2016-05-02", paramsCaptorValue.get("paramStartDate"));
        assertEquals("2016-05-08", paramsCaptorValue.get("paramEndDate"));
        assertEquals("2016W18", paramsCaptorValue.get("paramReportingPeriod"));
        assertNull(paramsCaptorValue.get("paramScheduleType"));
        assertNull(paramsCaptorValue.get("paramPreviousPeriods"));
    }
    @Test
    public void shouldCalculateReportingPeriodAndStartEndDatesForYearlyRecurringReports() throws Exception {
        String configFile = "some.json";
        String period = "2015";
        String startDate = "2015-01-01";
        String endDate = "2015-12-31";

        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("paramStartDate", startDate);
        jobDataMap.put("paramEndDate", endDate);
        jobDataMap.put("paramPeriodType", "Yearly");
        jobDataMap.put("paramFacilityId", "12345");
        jobDataMap.put("paramDatasetId", "abc123");
        jobDataMap.put("paramOrgUnitId", "10001");
        jobDataMap.put("paramConfigFile", configFile);
        jobDataMap.put("paramReportingPeriod", period);
        jobDataMap.put("paramScheduleType", "repeat");
        jobDataMap.put("paramPreviousPeriods", "2");

        Date date = DateUtil.parseDate("2016-05-16");
        DateTime currentDateTime = new DateTime(date);
        org.joda.time.DateTimeUtils.setCurrentMillisFixed(currentDateTime.getMillis());

        dhisDynamicReport.processAndPost(jobDataMap);

        ArgumentCaptor<Map> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(aqsFTLProcessor).process(eq(configFile), paramsCaptor.capture());
        Map paramsCaptorValue = paramsCaptor.getValue();
        assertEquals("2014-01-01", paramsCaptorValue.get("paramStartDate"));
        assertEquals("2014-12-31", paramsCaptorValue.get("paramEndDate"));
        assertEquals("2014", paramsCaptorValue.get("paramReportingPeriod"));
        assertNull(paramsCaptorValue.get("paramScheduleType"));
        assertNull(paramsCaptorValue.get("paramPreviousPeriods"));
    }
}