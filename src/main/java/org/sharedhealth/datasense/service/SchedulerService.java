package org.sharedhealth.datasense.service;

import org.apache.log4j.Logger;
import org.quartz.*;
import org.sharedhealth.datasense.export.dhis.Jobs.DHISDailyOPDIPDPostJob;
import org.sharedhealth.datasense.export.dhis.Jobs.DHISMonthlyColposcopyPostJob;
import org.sharedhealth.datasense.export.dhis.Jobs.DHISMonthlyEPIInfantPostJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

import static java.util.Arrays.asList;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

@Service
public class SchedulerService {
    private static final Logger logger = Logger.getLogger(SchedulerService.class);

    private Scheduler scheduler;
    private static final String DAILY_IPD_OPD_JOB = "dhis.daily.opdipd.post.job";
    private static final String MONTHLY_EPI_INFANT_JOB = "dhis.monthly.epi.infant.post.job";
    private static final String MONTHLY_COLPOSCOPY_JOB = "dhis.monthly.colcoscopy.post.job";

    public static final List<String> ALL_JOB_NAMES = asList(DAILY_IPD_OPD_JOB, MONTHLY_EPI_INFANT_JOB, MONTHLY_COLPOSCOPY_JOB);

    private Map<String, JobDetail> jobs;

    private final String NO_SUCH_REPORT_MESSAGE = "There are no such reports";

    @Autowired
    public SchedulerService(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.jobs = getAllJobs();
    }

    public String startJob(String jobName, String cronExpression, String reportParamKey, String reportParamValue) throws SchedulerException {
        if (!(ALL_JOB_NAMES.contains(jobName))) {
            logger.info(NO_SUCH_REPORT_MESSAGE);
            return NO_SUCH_REPORT_MESSAGE;
        }

        if (isJobAlreadyPresent(jobName)) {
            logger.info(String.format("The job %s was already running", jobName));

            return "The job is already running";
        }

        JobDetail jobDetail = jobs.get(jobName);
        if (null == jobDetail) {
            logger.info(NO_SUCH_REPORT_MESSAGE);
            return NO_SUCH_REPORT_MESSAGE;
        }

        jobDetail.getJobDataMap().put(reportParamKey, reportParamValue);

        String triggerName = jobName + ".trigger";
        CronTrigger trigger = getTrigger(triggerName, cronExpression, jobDetail);
        scheduler.scheduleJob(jobDetail, trigger);
        logger.info(String.format("Job %s starred", jobName));
        return "Job Started";
    }

    public String stopJob(String jobName) throws SchedulerException {
        if (!(ALL_JOB_NAMES.contains(jobName))) {
            logger.info(NO_SUCH_REPORT_MESSAGE);
            return NO_SUCH_REPORT_MESSAGE;
        }

        if (!isJobAlreadyPresent(jobName)) {
            logger.info(String.format("The job %s was not running", jobName));
            return "The job is not running";
        }

        Trigger trigger = getTriggerByJobName(jobName);
        if (null == trigger) {
            logger.error(String.format("Cannot find trigger for job %s to stop", jobName));
            return "cannot stop job";
        }

        scheduler.unscheduleJob(trigger.getKey());
        logger.info(String.format("Job %s stopped", jobName));
        return "Job Stopped";
    }

    public List<JobAttributes> getStoppedJobs() throws SchedulerException {
        List<JobAttributes> stoppedJobs = new ArrayList<>();
        for (String jobName : ALL_JOB_NAMES) {
            if (!isJobAlreadyPresent(jobName)) {
                stoppedJobs.add(new JobAttributes(jobName));
            }
        }
        return stoppedJobs;
    }

    public List<JobAttributes> getRunningJobs() throws SchedulerException {
        List<JobAttributes> runningJobs = new ArrayList<>();
        for (String jobName : ALL_JOB_NAMES) {
            if (isJobAlreadyPresent(jobName)) {
                runningJobs.add(getJobDetails(jobName));
            }
        }
        return runningJobs;
    }

    private JobAttributes getJobDetails(String jobName) throws SchedulerException {
        Trigger trigger = getTriggerByJobName(jobName);
        String cronExpression = ((CronTrigger) trigger).getCronExpression();
        JobDetail jobDetail = scheduler.getJobDetail(trigger.getJobKey());
        Map.Entry<String, Object> firstEntry = jobDetail.getJobDataMap().entrySet().iterator().next();
        return new JobAttributes(jobName, cronExpression, firstEntry.getKey(), (String) firstEntry.getValue());
    }

    private Trigger getTriggerByJobName(String jobName) throws SchedulerException {
        Trigger trigger = null;
        List<String> jobGroupNames = scheduler.getJobGroupNames();
        for (String jobGroupName : jobGroupNames) {
            Set<JobKey> jobKeys = scheduler.getJobKeys(jobGroupEquals(jobGroupName));
            for (JobKey jobKey : jobKeys) {
                if (jobKey.getName().equals(jobName)) {
                    trigger = scheduler.getTriggersOfJob(jobKey).get(0);
                }
            }
        }
        return trigger;
    }

    private String getJobName(Integer reportID) {
        try {
            return ALL_JOB_NAMES.get(reportID - 1);
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private boolean isJobAlreadyPresent(String jobName) throws SchedulerException {
        List<String> jobGroupNames = scheduler.getJobGroupNames();
        for (String jobGroupName : jobGroupNames) {
            for (JobKey jobKey : scheduler.getJobKeys(jobGroupEquals(jobGroupName))) {
                if (jobKey.getName().equals(jobName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, JobDetail> getAllJobs() {
        HashMap<String, JobDetail> jobs = new HashMap<>();
        jobs.put(DAILY_IPD_OPD_JOB, dhisDailyOPDIPDJob());
        jobs.put(MONTHLY_EPI_INFANT_JOB, dhisEPIInfantPostJob());
        jobs.put(MONTHLY_COLPOSCOPY_JOB, dhisColposcopyPostJob());
        return jobs;
    }

    private CronTrigger getTrigger(String triggerName, String cronExpression, JobDetail jobDetail) {
        CronTriggerFactoryBean triggerFactoryBean = new CronTriggerFactoryBean();
        triggerFactoryBean.setName(triggerName);
        triggerFactoryBean.setCronExpression(cronExpression);
        triggerFactoryBean.setJobDetail(jobDetail);
        try {
            triggerFactoryBean.afterPropertiesSet();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return triggerFactoryBean.getObject();
    }

    private JobDetail dhisDailyOPDIPDJob() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(DHISDailyOPDIPDPostJob.class);
        jobDetailFactoryBean.setName(DAILY_IPD_OPD_JOB);
        jobDetailFactoryBean.afterPropertiesSet();
        return jobDetailFactoryBean.getObject();
    }

    private JobDetail dhisEPIInfantPostJob() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(DHISMonthlyEPIInfantPostJob.class);
        jobDetailFactoryBean.setName(MONTHLY_EPI_INFANT_JOB);
        jobDetailFactoryBean.afterPropertiesSet();
        return jobDetailFactoryBean.getObject();
    }

    private JobDetail dhisColposcopyPostJob() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(DHISMonthlyColposcopyPostJob.class);
        jobDetailFactoryBean.setName(MONTHLY_COLPOSCOPY_JOB);
        jobDetailFactoryBean.afterPropertiesSet();
        return jobDetailFactoryBean.getObject();
    }
}
