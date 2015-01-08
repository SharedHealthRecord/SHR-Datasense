package org.sharedhealth.datasense.export.dhis;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

public class DHISDailyOPDIPDPostJobTest {

    @Test
    public void shouldParseReportingDateAsDate() {
        String reportingDate = "2015-01-07";
        assertEquals(reportingDate, DHISDailyOPDIPDPostJob.getReportingDate(reportingDate));
    }

    @Test
    public void shouldParseIntegerAsDate() {
        String dataParam = "0";
        Calendar calendar = Calendar.getInstance();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        assertEquals(today, DHISDailyOPDIPDPostJob.getReportingDate(dataParam));
    }

    @Test
    public void shouldGiveYesterdayAsDefault() {
        String dataParam = "invalid";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        assertEquals(yesterday, DHISDailyOPDIPDPostJob.getReportingDate(dataParam));
    }
}