package org.sharedhealth.datasense.export.dhis;

import org.junit.Test;
import org.sharedhealth.datasense.export.dhis.Jobs.DHISDailyOPDIPDPostJob;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;

public class DHISDailyOPDIPDPostJobTest {

    @Test
    public void shouldParseReportingDateAsDate() {
        String reportingDate = "2015-01-07";
        assertEquals(reportingDate, new DHISDailyOPDIPDPostJob().getReportingDate(reportingDate));
    }

    @Test
    public void shouldParseIntegerAsDate() {
        String dataParam = "0";
        Calendar calendar = Calendar.getInstance();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        assertEquals(today, new DHISDailyOPDIPDPostJob().getReportingDate(dataParam));
    }

    @Test
    public void shouldGiveYesterdayAsDefault() {
        String dataParam = "invalid";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        assertEquals(yesterday, new DHISDailyOPDIPDPostJob().getReportingDate(dataParam));
    }
}