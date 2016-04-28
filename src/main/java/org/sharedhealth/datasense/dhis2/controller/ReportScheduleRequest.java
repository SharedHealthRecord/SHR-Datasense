package org.sharedhealth.datasense.dhis2.controller;


import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.datasense.dhis2.model.DHISOrgUnitConfig;
import org.sharedhealth.datasense.util.DateUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReportScheduleRequest {
    private static final String DATE_FMT_DD_MM_YYYY = "dd/MM/yyyy";
    public static final String DAILY_PERIOD_TYPE = "Daily";
    public static final String MONTHLY_PERIOD_TYPE = "Monthly";
    public static final String YEARLY_PERIOD_TYPE = "Yearly";
    private static final String WEEKLY_PERIOD_TYPE = "Weekly";
    private static final String QUARTERLY_PERIOD_TYPE = "Quarterly";
    public static final String[] SUPPORTED_PERIOD_TYPES = {DAILY_PERIOD_TYPE, MONTHLY_PERIOD_TYPE, QUARTERLY_PERIOD_TYPE, YEARLY_PERIOD_TYPE};

    private List<String> selectedFacilities = new ArrayList<>();
    private String periodType;
    private String startDate;
    private String datasetId;
    private String scheduleType;
    private String endDate;
    private ReportPeriod reportPeriod;
    private String datasetName;
    private Integer configId;

    public ReportScheduleRequest() {
    }

    public List<String> getSelectedFacilities() {
        return selectedFacilities;
    }

    public void setSelectedFacilities(List<String> selectedFacilities) {
        this.selectedFacilities = selectedFacilities;
    }

    public String getPeriodType() {
        return periodType;
    }

    public void setPeriodType(String periodType) {
        this.periodType = periodType;
    }



    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public Integer getConfigId() {
        return configId;
    }

    public void setConfigId(Integer configId) {
        this.configId = configId;
    }


    private Calendar fromDateString(String dateString) {
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


    public ReportPeriod reportPeriod() {
        if (this.reportPeriod != null) {
            return this.reportPeriod;
        }

        if (StringUtils.isBlank(periodType)) {
            return null;
        }

        if (StringUtils.isBlank(this.startDate)) {
            return null;
        }

        this.reportPeriod = createReportPeriod();
        return this.reportPeriod;
    }

    private ReportPeriod createReportPeriod() {
        if (periodType.equalsIgnoreCase(DAILY_PERIOD_TYPE)) {
            return new DailyReportPeriod(startDate);
        }

        if (periodType.equalsIgnoreCase(MONTHLY_PERIOD_TYPE)) {
            return new MonthlyReportPeriod(startDate);
        }

        if (periodType.equalsIgnoreCase(YEARLY_PERIOD_TYPE)) {
            return new YearlyReportPeriod(startDate);
        }

        if (periodType.equalsIgnoreCase(WEEKLY_PERIOD_TYPE)) {
            return new WeeklyReportPeriod(startDate);
        }

        if (periodType.equalsIgnoreCase(QUARTERLY_PERIOD_TYPE)) {
            return new QuarterlyReportPeriod(startDate);
        }
        return new NotImplementedPeriod(startDate);
    }


    public abstract class ReportPeriod {
        protected Calendar reportCalendar;

        public ReportPeriod(String reportStartDate) {
            this.reportCalendar = fromDateString(reportStartDate);
        }

        public abstract String period();
        public abstract String startDate();
        public abstract String endDate();
    }

    public class NotImplementedPeriod extends ReportPeriod {
        public NotImplementedPeriod(String reportStartDate) {
            super(reportStartDate);
        }
        @Override
        public String period() {
            return null;
        }
        @Override
        public String startDate() {
            return null;
        }
        @Override
        public String endDate() {
            return null;
        }
    }

    public class DailyReportPeriod extends ReportPeriod {
        public DailyReportPeriod(String reportStartDate) {
            super(reportStartDate);
        }

        @Override
        public String period() {
            return String.format("%04d%02d%02d", reportCalendar.get(Calendar.YEAR), reportCalendar.get(Calendar.MONTH) + 1, reportCalendar.get(Calendar.DAY_OF_MONTH));
        }
        @Override
        public String startDate() {
            return String.format("%04d-%02d-%02d", reportCalendar.get(Calendar.YEAR), reportCalendar.get(Calendar.MONTH) + 1, reportCalendar.get(Calendar.DAY_OF_MONTH));
        }
        @Override
        public String endDate() {
            return startDate();
        }
    }

    public class MonthlyReportPeriod extends ReportPeriod {
        public MonthlyReportPeriod(String reportStartDate) {
            super(reportStartDate);
        }

        @Override
        public String period() {
            return String.format("%04d%02d", reportCalendar.get(Calendar.YEAR), reportCalendar.get(Calendar.MONTH) + 1);
        }
        @Override
        public String startDate() {
            return String.format("%04d-%02d-%02d", reportCalendar.get(Calendar.YEAR), reportCalendar.get(Calendar.MONTH) + 1, 1);
        }
        @Override
        public String endDate() {
            int endDayOfMonth = reportCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            return String.format("%04d-%02d-%02d", reportCalendar.get(Calendar.YEAR), reportCalendar.get(Calendar.MONTH) + 1, endDayOfMonth);
        }
    }

    public class YearlyReportPeriod extends ReportPeriod {
        public YearlyReportPeriod(String reportStartDate) {
            super(reportStartDate);
        }

        @Override
        public String period() {
            return String.format("%04d", reportCalendar.get(Calendar.YEAR));
        }
        @Override
        public String startDate() {
            return String.format("%04d-%02d-%02d", reportCalendar.get(Calendar.YEAR), 1, 1);
        }
        @Override
        public String endDate() {
            return String.format("%04d-%02d-%02d", reportCalendar.get(Calendar.YEAR), 12, 31);
        }
    }

    public class WeeklyReportPeriod extends ReportPeriod {
        public WeeklyReportPeriod(String reportStartDate) {
            super(reportStartDate);
        }

        @Override
        public String period() {
            return String.format("%04dW%d", reportCalendar.get(Calendar.YEAR), reportCalendar.get(Calendar.WEEK_OF_YEAR));
        }
        @Override
        public String startDate() {
            Calendar first = (Calendar) reportCalendar.clone();
            first.setFirstDayOfWeek(Calendar.MONDAY);
            first.add(Calendar.DAY_OF_WEEK, first.getFirstDayOfWeek() - first.get(Calendar.DAY_OF_WEEK));
            //return String.format("%04d-%02d-%02d", reportCalendar.get(Calendar.YEAR), 1, 1);
            return new SimpleDateFormat(DateUtil.SIMPLE_DATE_FORMAT).format(first.getTime());
        }
        @Override
        public String endDate() {
            Calendar first = (Calendar) reportCalendar.clone();
            first.setFirstDayOfWeek(Calendar.MONDAY);
            first.add(Calendar.DAY_OF_WEEK, first.getFirstDayOfWeek() - first.get(Calendar.DAY_OF_WEEK));
            Calendar last = (Calendar) first.clone();
            last.setFirstDayOfWeek(Calendar.MONDAY);
            last.add(Calendar.DAY_OF_YEAR, 6);
            return new SimpleDateFormat(DateUtil.SIMPLE_DATE_FORMAT).format(last.getTime());
        }
    }

    public class QuarterlyReportPeriod extends ReportPeriod {
        public QuarterlyReportPeriod(String reportStartDate) {
            super(reportStartDate);
        }

        @Override
        public String period() {
            int quarterNumber = getQuarterNumber();
            return String.format("%04dQ%d", reportCalendar.get(Calendar.YEAR), quarterNumber);
        }

        private int getQuarterNumber() {
            int month = reportCalendar.get(Calendar.MONTH);
            return month/3 + 1;
        }

        @Override
        public String startDate() {
            Calendar first = (Calendar) reportCalendar.clone();
            int quarterNumber = getQuarterNumber();
            int startMonth = quarterNumber*3 - 2;
            return String.format("%04d-%02d-%02d", first.get(Calendar.YEAR), startMonth, 1);
        }
        @Override
        public String endDate() {
            Calendar last = (Calendar) reportCalendar.clone();
            int quarterNumber = getQuarterNumber();
            int endMonth = quarterNumber * 3;
            last.set(Calendar.MONTH, endMonth - 1);
            int endDayOfMonth = last.getActualMaximum(Calendar.DAY_OF_MONTH);
            return String.format("%04d-%02d-%02d", last.get(Calendar.YEAR), last.get(Calendar.MONTH) + 1, endDayOfMonth);
        }

    }
}
