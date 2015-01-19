package org.sharedhealth.datasense.export.dhis;

import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static junit.framework.Assert.assertEquals;
import static org.sharedhealth.datasense.export.dhis.DHISMonthlyEPIInfantPostJob.getReportingMonth;

public class DHISMonthlyEPIInfantPostJobTest {

    @Test
    public void shouldParseGivenStringAsReportingMonth() throws Exception {
        String reportingMonth = "2014-01";
        assertEquals(reportingMonth, getReportingMonth(reportingMonth));
    }

    @Test
    public void shouldParseIntegerAndGiveReportingMonth() throws Exception {
        String reportingMonth = "2014-11";
        assertEquals(reportingMonth, getReportingMonth("-2"));
    }

    @Test
    public void shouldGiveLastMonthAsDefaultReportingMonth() throws Exception {
        String monthParam = "invalid";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM").format(calendar.getTime());
        Assert.assertEquals(yesterday, getReportingMonth(monthParam));
    }
}