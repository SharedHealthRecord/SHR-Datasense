package org.sharedhealth.datasense.dhis2.controller;

import org.sharedhealth.datasense.util.DateUtil;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import static org.sharedhealth.datasense.dhis2.controller.ReportScheduleRequest.*;
import static org.sharedhealth.datasense.util.DateUtil.toGivenFormatString;

public class ReportFactory {
    public static ReportScheduleRequest.ReportPeriod createReportPeriod(String startDate, String scheduleStartDate,
                                                                        String periodType, String scheduleType,
                                                                        Integer previousPeriods) {
        ReportScheduleRequest reportScheduleRequest = new ReportScheduleRequest();
        if (periodType.equalsIgnoreCase(DAILY_PERIOD_TYPE)) {
            startDate = calculateStartDateForRecurring(startDate, scheduleStartDate, scheduleType,
                    Calendar.DATE, previousPeriods);
            return reportScheduleRequest.new DailyReportPeriod(startDate);
        }

        if (periodType.equalsIgnoreCase(MONTHLY_PERIOD_TYPE)) {
            startDate = calculateStartDateForRecurring(startDate, scheduleStartDate, scheduleType,
                    Calendar.MONTH, previousPeriods);
            return reportScheduleRequest.new MonthlyReportPeriod(startDate);
        }

        if (periodType.equalsIgnoreCase(YEARLY_PERIOD_TYPE)) {
            startDate = calculateStartDateForRecurring(startDate, scheduleStartDate, scheduleType,
                    Calendar.YEAR, previousPeriods);
            return reportScheduleRequest.new YearlyReportPeriod(startDate);
        }

        if (periodType.equalsIgnoreCase(WEEKLY_PERIOD_TYPE)) {
            startDate = calculateStartDateForRecurring(startDate, scheduleStartDate, scheduleType,
                    Calendar.WEEK_OF_YEAR, previousPeriods);
            return reportScheduleRequest.new WeeklyReportPeriod(startDate);
        }

        if (periodType.equalsIgnoreCase(QUARTERLY_PERIOD_TYPE)) {
            previousPeriods = previousPeriods != null ? 3 * previousPeriods : null;
            startDate = calculateStartDateForRecurring(startDate, scheduleStartDate, scheduleType,
                    Calendar.MONTH, previousPeriods);
            return reportScheduleRequest.new QuarterlyReportPeriod(startDate);
        }
        return reportScheduleRequest.new NotImplementedPeriod(startDate);
    }

    private static String calculateStartDateForRecurring(String startDate, String scheduleStartDate,
                                                         String scheduleType, Integer unitForPeriod, Integer periodsToReduce) {
        if (SCHEDULE_TYPE_ONCE.equalsIgnoreCase(scheduleType)) {
            return startDate;
        }
        Calendar calendar = fromDateString(scheduleStartDate);
        calendar.add(unitForPeriod, -periodsToReduce);
        return toGivenFormatString(calendar.getTime(), DateUtil.DATE_FMT_DD_MM_YYYY);
    }

    private static Calendar fromDateString(String dateString) {
        try {
            Date date = DateUtil.parseDate(dateString, DateUtil.DATE_FMT_DD_MM_YYYY);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            return c;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


}
