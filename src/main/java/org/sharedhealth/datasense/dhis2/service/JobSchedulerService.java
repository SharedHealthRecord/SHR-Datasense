package org.sharedhealth.datasense.dhis2.service;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.sharedhealth.datasense.dhis2.controller.ReportScheduleRequest;
import org.sharedhealth.datasense.dhis2.model.DHISOrgUnitConfig;
import org.sharedhealth.datasense.dhis2.model.DHISReportConfig;
import org.sharedhealth.datasense.dhis2.model.DatasetJobSchedule;
import org.sharedhealth.datasense.dhis2.repository.DHISConfigDao;
import org.sharedhealth.datasense.export.dhis.Jobs.DHISQuartzJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.quartz.TriggerBuilder.newTrigger;

@Service
public class JobSchedulerService {
    private static final Logger logger = Logger.getLogger(JobSchedulerService.class);
    private Scheduler scheduler;
    private DHISConfigDao dhisMapDao;


    @Autowired
    public JobSchedulerService(Scheduler scheduler, DHISConfigDao dhisMapDao) {
        this.scheduler = scheduler;
        this.dhisMapDao = dhisMapDao;
    }

    public void scheduleJob(ReportScheduleRequest scheduleRequest) throws SchedulerException {
        if (scheduleRequest.getScheduleType().equalsIgnoreCase("once")) {
            scheduleOnce(scheduleRequest);
        }
    }

    private void scheduleOnce(ReportScheduleRequest scheduleRequest)  {
        String datasetId = scheduleRequest.getDatasetId();
        DHISReportConfig configForDataset = dhisMapDao.getMappedConfigForDataset(datasetId);

        for (String facilityId : scheduleRequest.getSelectedFacilities()) {
            JobDetailFactoryBean jobFactory = new JobDetailFactoryBean();
            jobFactory.setJobClass(DHISQuartzJob.class);
            DHISOrgUnitConfig orgUnitConfig = dhisMapDao.findOrgUnitConfigFor(facilityId);

            String jobName = getJobName(datasetId, facilityId, scheduleRequest.reportPeriod().period());
            jobFactory.setName(jobName);
            jobFactory.setGroup(datasetId);
            jobFactory.afterPropertiesSet();

            JobDetail jobDetail = jobFactory.getObject();
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.put("paramStartDate", scheduleRequest.reportPeriod().startDate());
            jobDataMap.put("paramEndDate", scheduleRequest.reportPeriod().endDate());
            jobDataMap.put("paramPeriodType", scheduleRequest.getPeriodType());
            jobDataMap.put("paramFacilityId", facilityId);
            jobDataMap.put("paramDatasetId", datasetId);
            jobDataMap.put("paramOrgUnitId", orgUnitConfig.getOrgUnitId());
            jobDataMap.put("paramConfigFile", configForDataset.getConfigFile());
            jobDataMap.put("paramReportingPeriod", scheduleRequest.reportPeriod().period());

            String datasetName =  scheduleRequest.getDatasetName();

            String triggerName = jobName + "-TRIGGER";
            SimpleTrigger trigger = (SimpleTrigger) newTrigger()
                                        .withIdentity(triggerName, datasetName)
                                        .startAt(afterSecs(10)).build();
            //CronTrigger trigger = getTrigger(triggerName, cronExpression, jobDetail);
            try {
                if (!scheduler.checkExists(trigger.getKey())) {
                    scheduler.scheduleJob(jobDetail, trigger);
                } else {
                    logger.debug(String.format("A schedule already exists for this period, report and facility configuration."));
                    throw new RuntimeException("A schedule already exists for this period, report and facility configuration.");
                }
            } catch (SchedulerException e) {
                logger.debug("Could not schedule job. SchedulerException thrown.", e);
                throw new RuntimeException("Could not schedule job. error : " + e.getMessage(), e);
            }
            logger.info(String.format("Job %s starred", jobName));

        }
    }

    private Date afterSecs(int seconds) {
        Date date = new Date();
        java.util.Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.SECOND, seconds);
        return c.getTime();
    }

    private String getJobName(String datasetId, String facilityId, String period) {
        return datasetId + "-" + facilityId + "-" + period;
    }


    private String identifyFacilityForJob(String jobName) {
        String[] parts = jobName.split("-");
        return parts[1];
    }

    private String identifyPeriodForJob(String jobName) {
        String[] parts = jobName.split("-");
        return parts[2];
    }

    public List<DatasetJobSchedule> findAllJobsForDataset(String datasetId) throws SchedulerException {
        DHISReportConfig configForDataset = dhisMapDao.getMappedConfigForDataset(datasetId);
        List<DatasetJobSchedule> reportSchedules = new ArrayList<DatasetJobSchedule>();
        if (configForDataset == null) {
            return reportSchedules;
        }

        List<String> jobGroupNames = scheduler.getJobGroupNames();
        for (String groupName : jobGroupNames) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                String jobName = jobKey.getName();
                if (!jobName.startsWith(datasetId + "-")) {
                    continue;
                }

                String facilityId = identifyFacilityForJob(jobName);
                String periodForJob = identifyPeriodForJob(jobName);

                String jobGroup = jobKey.getGroup();
                //get job's trigger
                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                for (Trigger trigger : triggers) {
                    DatasetJobSchedule reportSchedule = new DatasetJobSchedule();
                    System.out.println("[jobName] : " + jobName + " [groupName] : " + jobGroup + " - " + trigger.getNextFireTime());
                    reportSchedule.setDatasetName(configForDataset.getDatasetName());
                    reportSchedule.setFacilityId(facilityId);
                    reportSchedule.setPeriod(periodForJob);
                    reportSchedule.setNextFireTime(toNotNullDateString(trigger.getNextFireTime()));
                    reportSchedule.setPrevFireTime(toNotNullDateString(trigger.getPreviousFireTime()));
                    reportSchedule.setStartTime(toNotNullDateString(trigger.getStartTime()));
                    reportSchedule.setEndTime(toNotNullDateString(trigger.getEndTime()));
                    reportSchedules.add(reportSchedule);

                }
            }

        }

        return reportSchedules;
    }

    private String toNotNullDateString(Date date) {
        return (date != null) ? date.toString() : "";
    }


}
