package org.sharedhealth.datasense.dhis2.controller;


import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ReportScheduleRequestTest {

    @Test
    public void shouldSetDailyReportPeriod() {
        ReportScheduleRequest request = new ReportScheduleRequest();
        request.setPeriodType("Daily");
        request.setStartDate("21/02/2015");
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
        ReportScheduleRequest.ReportPeriod reportPeriod = request.reportPeriod();
        assertEquals("2015W2", reportPeriod.period());
        assertEquals("2015-01-05", reportPeriod.startDate());
        assertEquals("2015-01-11", reportPeriod.endDate());
    }

    @Test
    public void shouldSetQuarterlyReportPeriod() {
        ReportScheduleRequest request = new ReportScheduleRequest();
        request.setPeriodType("Quarterly");
        request.setStartDate("09/01/2015");
        ReportScheduleRequest.ReportPeriod reportPeriod = request.reportPeriod();
        assertEquals("2015Q1", reportPeriod.period());
        assertEquals("2015-01-01", reportPeriod.startDate());
        assertEquals("2015-03-31", reportPeriod.endDate());

        request = new ReportScheduleRequest();
        request.setPeriodType("Quarterly");
        request.setStartDate("28/02/2015");
        reportPeriod = request.reportPeriod();
        assertEquals("2015Q1", reportPeriod.period());
        assertEquals("2015-01-01", reportPeriod.startDate());
        assertEquals("2015-03-31", reportPeriod.endDate());

        request = new ReportScheduleRequest();
        request.setPeriodType("Quarterly");
        request.setStartDate("28/04/2015");
        reportPeriod = request.reportPeriod();
        assertEquals("2015Q2", reportPeriod.period());
        assertEquals("2015-04-01", reportPeriod.startDate());
        assertEquals("2015-06-30", reportPeriod.endDate());

        request = new ReportScheduleRequest();
        request.setPeriodType("Quarterly");
        request.setStartDate("30/06/2015");
        reportPeriod = request.reportPeriod();
        assertEquals("2015Q2", reportPeriod.period());
        assertEquals("2015-04-01", reportPeriod.startDate());
        assertEquals("2015-06-30", reportPeriod.endDate());

        request = new ReportScheduleRequest();
        request.setPeriodType("Quarterly");
        request.setStartDate("28/11/2015");
        reportPeriod = request.reportPeriod();
        assertEquals("2015Q4", reportPeriod.period());
        assertEquals("2015-10-01", reportPeriod.startDate());
        assertEquals("2015-12-31", reportPeriod.endDate());
    }

    @Test
    public void shouldGenerateStartDateForRecurringScheduleRequests() throws Exception {
        ReportScheduleRequest request = new ReportScheduleRequest();
        request.setPeriodType("Daily");
        request.setScheduleStartDate("19/05/2016");
        request.setPreviousPeriods(3);
        ReportScheduleRequest.ReportPeriod reportPeriod = request.reportPeriod();
        assertEquals("20160516", reportPeriod.period());
        assertEquals("2016-05-16", reportPeriod.startDate());
        assertEquals("2016-05-16", reportPeriod.endDate());

        request = new ReportScheduleRequest();
        request.setPeriodType("Monthly");
        request.setScheduleStartDate("19/05/2016");
        request.setPreviousPeriods(3);
        reportPeriod = request.reportPeriod();
        assertEquals("201602", reportPeriod.period());
        assertEquals("2016-02-01", reportPeriod.startDate());
        assertEquals("2016-02-29", reportPeriod.endDate());

        request = new ReportScheduleRequest();
        request.setPeriodType("Quarterly");
        request.setScheduleStartDate("19/05/2016");
        request.setPreviousPeriods(3);
        reportPeriod = request.reportPeriod();
        assertEquals("2015Q3", reportPeriod.period());
        assertEquals("2015-07-01", reportPeriod.startDate());
        assertEquals("2015-09-30", reportPeriod.endDate());

        request = new ReportScheduleRequest();
        request.setPeriodType("Yearly");
        request.setScheduleStartDate("19/05/2016");
        request.setPreviousPeriods(3);
        reportPeriod = request.reportPeriod();
        assertEquals("2013", reportPeriod.period());
        assertEquals("2013-01-01", reportPeriod.startDate());
        assertEquals("2013-12-31", reportPeriod.endDate());

        // report when week start from 28th dec and ends 3rd jan and Schedule Start Date is 19th
        request = new ReportScheduleRequest();
        request.setPeriodType("Weekly");
        request.setScheduleStartDate("19/01/2016");
        request.setPreviousPeriods(3);
        reportPeriod = request.reportPeriod();
        assertEquals("2015W52", reportPeriod.period());
        assertEquals("2015-12-28", reportPeriod.startDate());
        assertEquals("2016-01-03", reportPeriod.endDate());

        // report when week start from 28th dec and ends 3rd jan and Schedule Start Date is 19th
        request = new ReportScheduleRequest();
        request.setPeriodType("Weekly");
        request.setScheduleStartDate("22/01/2016");
        request.setPreviousPeriods(3);
        reportPeriod = request.reportPeriod();
        assertEquals("2015W52", reportPeriod.period());
        assertEquals("2015-12-28", reportPeriod.startDate());
        assertEquals("2016-01-03", reportPeriod.endDate());
    }
}