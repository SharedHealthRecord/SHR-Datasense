package org.sharedhealth.datasense.controller;

import org.quartz.*;
import org.sharedhealth.datasense.export.dhis.Jobs.DHISDailyOPDIPDPostJob;
import org.sharedhealth.datasense.export.dhis.Jobs.DHISMonthlyColposcopyPostJob;
import org.sharedhealth.datasense.export.dhis.Jobs.DHISMonthlyEPIInfantPostJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

@Controller
@RequestMapping(value = "/scheduler")
public class SchedulerController {
    private Scheduler scheduler;
    private static final String DAILY_IPD_OPD_JOB = "dhis.daily.opdipd.post.job";
    private static final String MONTHLY_EPI_INFANT_JOB = "dhis.monthly.epi.infant.post.job";
    private static final String MONTHLY_COLPOSCOPY_JOB = "dhis.monthly.colcoscopy.post.job";

    private Map<String, JobDetail> jobs;
    private List<String> jobNames;

    @Autowired
    public SchedulerController(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.jobNames = asList(DAILY_IPD_OPD_JOB, MONTHLY_EPI_INFANT_JOB, MONTHLY_COLPOSCOPY_JOB);
        this.jobs = getAllJobs();
    }

    private Map<String, JobDetail> getAllJobs() {
        HashMap<String, JobDetail> jobs = new HashMap<>();
        jobs.put(DAILY_IPD_OPD_JOB, dhisDailyOPDIPDJob());
        jobs.put(MONTHLY_EPI_INFANT_JOB, dhisEPIInfantPostJob());
        jobs.put(MONTHLY_COLPOSCOPY_JOB, dhisColposcopyPostJob());
        return jobs;
    }

    @RequestMapping(value = "/start")
    @ResponseBody
    public String startScheduler(
            @RequestParam(value = "reportId") Integer reportID,
            @RequestParam(value = "expression") String cronExpression,
            @RequestParam(value = "paramKey") String reportParamKey,
            @RequestParam(value = "paramValue") String reportParamValue)
            throws SchedulerException {

        String jobName = getJobName(reportID);
        if (null == jobName) return "There are no such reports";

        if (isJobAlreadyPresent(jobName)) {
            return "The job is already running";
        }

        JobDetail jobDetail = jobs.get(jobName);
        if (null == jobDetail) return "There are no such reports";

        jobDetail.getJobDataMap().put(reportParamKey, reportParamValue);

        String triggerName = jobName + ".trigger";
        CronTrigger trigger = getTrigger(triggerName, cronExpression, jobDetail);
        scheduler.scheduleJob(jobDetail, trigger);
        return "Job Started";
    }

    @RequestMapping(value = "/stop", method = RequestMethod.GET)
    @ResponseBody
    public String stopScheduler(@RequestParam(value = "reportId") Integer reportID) throws SchedulerException {
        String jobName = getJobName(reportID);
        if (null == jobName) return "There are no such reports";

        if (!isJobAlreadyPresent(jobName)) {
            return "The job is not running";
        }

        Trigger trigger = getTriggerByJobName(jobName);
        if (null == trigger) return "cannot stop job";

        scheduler.unscheduleJob(trigger.getKey());
        return "Job Stopped";
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
            return jobNames.get(reportID - 1);
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
