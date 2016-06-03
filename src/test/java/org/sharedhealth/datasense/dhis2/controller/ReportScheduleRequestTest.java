package org.sharedhealth.datasense.dhis2.controller;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.sharedhealth.datasense.dhis2.controller.ReportScheduleRequest.SCHEDULE_TYPE_ONCE;
import static org.sharedhealth.datasense.dhis2.controller.ReportScheduleRequest.SCHEDULE_TYPE_REPEAT;


public class ReportScheduleRequestTest {

    @Test
    public void shouldSetDailyReportPeriod() {
        ReportScheduleRequest request = new ReportScheduleRequest();
        request.setPeriodType("Daily");
        request.setStartDate("21/02/2015");
        request.setScheduleType(SCHEDULE_TYPE_ONCE);
        ReportScheduleRequest.ReportPeriod reportPeriod = request.reportPeriod();
        assertEquals("20150221", reportPeriod.period());
        assertEquals("2015-02-21", reportPeriod.startDate());
        assertEquals("2015-02-21", reportPeriod.endDate());
    }

    @Test
    public void shouldSetMonthlyReportPeriod() {
        ReportScheduleRequest request = new ReportScheduleRequest();
        request.setPeriodType("Monthly");
        request.setStartDate("21/02/2015");
        request.setScheduleType(SCHEDULE_TYPE_ONCE);
        ReportScheduleRequest.ReportPeriod reportPeriod = request.reportPeriod();
        assertEquals("201502", reportPeriod.period());
        assertEquals("2015-02-01", reportPeriod.startDate());
        assertEquals("2015-02-28", reportPeriod.endDate());
    }

    @Test
    public void shouldSetYearlyReportPeriod() {
        ReportScheduleRequest request = new ReportScheduleRequest();
        request.setPeriodType("Yearly");
        request.setStartDate("21/02/2015");
        request.setScheduleType(SCHEDULE_TYPE_ONCE);
        ReportScheduleRequest.ReportPeriod reportPeriod = request.reportPeriod();
        assertEquals("2015", reportPeriod.period());
        assertEquals("2015-01-01", reportPeriod.startDate());
        assertEquals("2015-12-31", reportPeriod.endDate());
    }

    @Test
    public void shouldSetWeeklyReportPeriod() {
        ReportScheduleRequest request = new ReportScheduleRequest();
        request.setPeriodType("Weekly");
        request.setStartDate("09/01/2015");
        request.setScheduleType(SCHEDULE_TYPE_ONCE);
        ReportScheduleRequest.ReportPeriod reportPeriod = request.reportPeriod();
        assertEquals("2015W2", reportPeriod.period());
        assertEquals("2015-01-05", reportPeriod.startDate());
        assertEquals("2015-01-11", reportPeriod.endDate());

        request = new ReportScheduleRequest();
        request.setPeriodType("Weekly");
        request.setStartDate("30/12/2014");
        request.setScheduleType(SCHEDULE_TYPE_ONCE);
        reportPeriod = request.reportPeriod();
        assertEquals("2015W1", reportPeriod.period());
        assertEquals("2014-12-29", reportPeriod.startDate());
        assertEquals("2015-01-04", reportPeriod.endDate());

        request = new ReportScheduleRequest();
        request.setPeriodType("Weekly");
        request.setStartDate("03/01/2015");
        request.setScheduleType(SCHEDULE_TYPE_ONCE);
        reportPeriod = request.reportPeriod();
        assertEquals("2015W1", reportPeriod.period());
        assertEquals("2014-12-29", reportPeriod.startDate());
        assertEquals("2015-01-04", reportPeriod.endDate());
    }

    @Test
    public void shouldSetQuarterlyReportPeriod() {
        ReportScheduleRequest request = new ReportScheduleRequest();
        request.setPeriodType("Quarterly");
        request.setStartDate("09/01/2015");
        request.setScheduleType(SCHEDULE_TYPE_ONCE);
        ReportScheduleRequest.ReportPeriod reportPeriod = request.reportPeriod();
        assertEquals("2015Q1", reportPeriod.period());
        assertEquals("2015-01-01", reportPeriod.startDate());
        assertEquals("2015-03-31", reportPeriod.endDate());

        request = new ReportScheduleRequest();
        request.setPeriodType("Quarterly");
        request.setStartDate("28/02/2015");
        request.setScheduleType(SCHEDULE_TYPE_ONCE);
        reportPeriod = request.reportPeriod();
        assertEquals("2015Q1", reportPeriod.period());
        assertEquals("2015-01-01", reportPeriod.startDate());
        assertEquals("2015-03-31", reportPeriod.endDate());

        request = new ReportScheduleRequest();
        request.setPeriodType("Quarterly");
        request.setStartDate("28/04/2015");
        request.setScheduleType(SCHEDULE_TYPE_ONCE);
        reportPeriod = request.reportPeriod();
        assertEquals("2015Q2", reportPeriod.period());
        assertEquals("2015-04-01", reportPeriod.startDate());
        assertEquals("2015-06-30", reportPeriod.endDate());

        request = new ReportScheduleRequest();
        request.setPeriodType("Quarterly");
        request.setStartDate("30/06/2015");
        request.setScheduleType(SCHEDULE_TYPE_ONCE);
        reportPeriod = request.reportPeriod();
        assertEquals("2015Q2", reportPeriod.period());
        assertEquals("2015-04-01", reportPeriod.startDate());
        assertEquals("2015-06-30", reportPeriod.endDate());

        request = new ReportScheduleRequest();
        request.setPeriodType("Quarterly");
        request.setStartDate("28/11/2015");
        request.setScheduleType(SCHEDULE_TYPE_ONCE);
        reportPeriod = request.reportPeriod();
        assertEquals("2015Q4", reportPeriod.period());
        assertEquals("2015-10-01", reportPeriod.startDate());
        assertEquals("2015-12-31", reportPeriod.endDate());
    }

    @Test
    public void shouldGenerateStartDateForDailyRecurringScheduleRequests() throws Exception {
        ReportScheduleRequest request = new ReportScheduleRequest();
        request.setPeriodType("Daily");
        request.setScheduleStartDate("19/05/2016");
        request.setScheduleType(SCHEDULE_TYPE_REPEAT);
        request.setPreviousPeriods(3);
        ReportScheduleRequest.ReportPeriod reportPeriod = request.reportPeriod();
        assertEquals("20160516", reportPeriod.period());
        assertEquals("2016-05-16", reportPeriod.startDate());
        assertEquals("2016-05-16", reportPeriod.endDate());
    }

    @Test
    public void shouldGenerateStartDateForQuaterlyRecurringScheduleRequests() throws Exception {
        ReportScheduleRequest request = new ReportScheduleRequest();
        request.setPeriodType("Quarterly");
        request.setScheduleStartDate("19/05/2016");
        request.setPreviousPeriods(1);
        request.setScheduleType(SCHEDULE_TYPE_REPEAT);
        ReportScheduleRequest.ReportPeriod reportPeriod = request.reportPeriod();
        assertEquals("2016Q1", reportPeriod.period());
        assertEquals("2016-01-01", reportPeriod.startDate());
        assertEquals("2016-03-31", reportPeriod.endDate());
    }

    @Test
    public void shouldGenerateStartDateForMonthlyRecurringScheduleRequests() throws Exception {
        ReportScheduleRequest request = new ReportScheduleRequest();
        request.setPeriodType("Monthly");
        request.setScheduleStartDate("19/05/2016");
        request.setPreviousPeriods(3);
        request.setScheduleType(SCHEDULE_TYPE_REPEAT);
        ReportScheduleRequest.ReportPeriod reportPeriod = request.reportPeriod();
        assertEquals("201602", reportPeriod.period());
        assertEquals("2016-02-01", reportPeriod.startDate());
        assertEquals("2016-02-29", reportPeriod.endDate());
    }

    @Test
    public void shouldGenerateStartDateForYearlyRecurringScheduleRequests() throws Exception {
        ReportScheduleRequest request = new ReportScheduleRequest();
        request.setPeriodType("Yearly");
        request.setScheduleStartDate("19/05/2016");
        request.setPreviousPeriods(2);
        request.setScheduleType(SCHEDULE_TYPE_REPEAT);
        ReportScheduleRequest.ReportPeriod reportPeriod = request.reportPeriod();
        assertEquals("2014", reportPeriod.period());
        assertEquals("2014-01-01", reportPeriod.startDate());
        assertEquals("2014-12-31", reportPeriod.endDate());
    }

    @Test
    public void shouldGenerateStartDateForWeeklyRecurringScheduleRequests() throws Exception {
        // report when week start from 28th dec and ends 3rd jan and Schedule Start Date is 19th
        ReportScheduleRequest request = new ReportScheduleRequest();
        request.setPeriodType("Weekly");
        request.setScheduleStartDate("19/01/2016");
        request.setPreviousPeriods(3);
        request.setScheduleType(SCHEDULE_TYPE_REPEAT);
        ReportScheduleRequest.ReportPeriod reportPeriod = request.reportPeriod();
        assertEquals("2016W1", reportPeriod.period());
        assertEquals("2015-12-28", reportPeriod.startDate());
        assertEquals("2016-01-03", reportPeriod.endDate());

        // report when week start from 28th dec and ends 3rd jan and Schedule Start Date is 19th
        request = new ReportScheduleRequest();
        request.setPeriodType("Weekly");
        request.setScheduleStartDate("22/01/2016");
        request.setPreviousPeriods(3);
        request.setScheduleType(SCHEDULE_TYPE_REPEAT);
        reportPeriod = request.reportPeriod();
        assertEquals("2016W1", reportPeriod.period());
        assertEquals("2015-12-28", reportPeriod.startDate());
        assertEquals("2016-01-03", reportPeriod.endDate());
    }
}