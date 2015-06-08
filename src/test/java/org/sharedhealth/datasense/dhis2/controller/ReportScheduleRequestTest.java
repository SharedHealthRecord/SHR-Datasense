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
        assertEquals("2015-01-05", reportPeriod.startDate());
        assertEquals("2015-01-11", reportPeriod.endDate());
        assertEquals("2015W2", reportPeriod.period());
    }

}