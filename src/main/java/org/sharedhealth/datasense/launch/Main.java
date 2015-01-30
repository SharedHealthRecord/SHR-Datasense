package org.sharedhealth.datasense.launch;


import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.sharedhealth.datasense.client.ShrWebClient;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.export.dhis.DHISDailyOPDIPDPostJob;
import org.sharedhealth.datasense.export.dhis.DHISMonthlyEPIInfantPostJob;
import org.sharedhealth.datasense.export.dhis.report.DHISDailyOPDIPDReport;
import org.sharedhealth.datasense.export.dhis.report.DHISMonthlyEPIInfantReport;
import org.sharedhealth.datasense.feeds.encounters.EncounterEventWorker;
import org.sharedhealth.datasense.feeds.tr.ConceptEventWorker;
import org.sharedhealth.datasense.feeds.tr.ReferenceTermEventWorker;
import org.sharedhealth.datasense.scheduler.jobs.TrReferenceTermSyncJob;
import org.sharedhealth.datasense.scheduler.jobs.CatchmentEncounterCrawlerJob;
import org.sharedhealth.datasense.scheduler.jobs.TrConceptSyncJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.sql.DataSource;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.valueOf;
import static java.lang.System.getenv;

@Configuration
@Import({DatabaseConfig.class})
@ComponentScan(basePackages = {"org.sharedhealth.datasense.config",
        "org.sharedhealth.datasense.processor",
        "org.sharedhealth.datasense.feeds",
        "org.sharedhealth.datasense.repository",
        "org.sharedhealth.datasense.client",
        "org.sharedhealth.datasense.handler",
        "org.sharedhealth.datasense.export.dhis",
        "org.sharedhealth.datasense.security",
        "org.sharedhealth.datasense.util"
})
public class Main {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DataSourceTransactionManager txmanager;

    @Autowired
    private DatasenseProperties properties;

    @Autowired
    private EncounterEventWorker encounterEventWorker;

    @Autowired
    private DHISDailyOPDIPDReport dhisDailyOPDIPDReport;

    @Autowired
    private DHISMonthlyEPIInfantReport dhisMonthlyEPIInfantReport;

    @Autowired
    private ShrWebClient shrWebClient;

    @Autowired
    private ConceptEventWorker conceptEventWorker;

    @Autowired
    private ReferenceTermEventWorker referenceTermEventWorker;
    private String CATCHMENT_ENCOUNTER_SYNC_JOB = "catchment.encounter.crawler.job";
    private String CONCEPT_SYNC_JOB = "tr.concept.sync.job";
    private String REF_TERM_SYNC_JOB = "tr.reference.term.sync.job";

    private String CATCHMENT_ENCOUNTER_SYNC_TRIGGER = "catchment.encounter.crawler.job.trigger";
    private String CONCEPT_SYNC_TRIGGER = "tr.concept.sync.job.trigger";
    private String REF_TERM_SYNC_TRIGGER = "tr.reference.term.sync.job.trigger";
    private String DAILY_IPD_OPD_TRIGGER = "dhis.daily.opdipd.post.job.trigger";
    private String MONTHLY_EPI_INFANT_TRIGGER = "dhis.monthly.epi.infant.post.job.trigger";

    @Bean
    public EmbeddedServletContainerFactory getFactory() {
        Map<String, String> env = getenv();
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        factory.addInitializers(new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
                ServletRegistration.Dynamic shr = servletContext.addServlet("shr", DispatcherServlet.class);
                shr.addMapping("/");
                shr.setInitParameter("contextClass", "org.springframework.web.context.support" +
                        ".AnnotationConfigWebApplicationContext");
            }
        });
        String bdshr_port = env.get("DATASENSE_PORT");
        factory.setPort(valueOf(bdshr_port));
        return factory;
    }

    @Bean
    public SchedulerFactoryBean scheduler() {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setDataSource(dataSource);
        schedulerFactoryBean.setTransactionManager(txmanager);

        schedulerFactoryBean.setConfigLocation(new ClassPathResource("db/quartz.properties"));
        schedulerFactoryBean.setJobFactory(jobFactory());


        schedulerFactoryBean.setTriggers(getTriggers());
        schedulerFactoryBean.setApplicationContextSchedulerContextKey("applicationContext");
        schedulerFactoryBean.setSchedulerContextAsMap(schedulerContextMap());
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
        try {
            schedulerFactoryBean.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schedulerFactoryBean;
    }

    private Trigger[] getTriggers() {
        return new Trigger[]{
                getTrigger(CATCHMENT_ENCOUNTER_SYNC_TRIGGER, 10000, "0/30 * * * * ?", jobDetail(CatchmentEncounterCrawlerJob.class, CATCHMENT_ENCOUNTER_SYNC_JOB).getObject()),
                getTrigger(CONCEPT_SYNC_TRIGGER, 10000, "0/30 * * * * ?", jobDetail(TrConceptSyncJob.class, CONCEPT_SYNC_JOB).getObject()),
                getTrigger(REF_TERM_SYNC_TRIGGER, 10000, "0/30 * * * * ?", jobDetail(TrReferenceTermSyncJob.class, REF_TERM_SYNC_JOB).getObject()),
                getTrigger(DAILY_IPD_OPD_TRIGGER, 10000, "0 0/15 * * * ?", dhisDailyOPDIPDJob()),
                getTrigger(MONTHLY_EPI_INFANT_TRIGGER, 10000, "0 0/15 * * * ?", dhisEPIInfantPostJob())
        };
    }

    @Bean
    public JobFactory jobFactory() {
        SpringBeanJobFactory springBeanJobFactory = new SpringBeanJobFactory();
        return springBeanJobFactory;
    }

    @Bean
    public Map<String, Object> schedulerContextMap() {
        HashMap<String, Object> ctx = new HashMap<>();
        ctx.put("txMgr", txmanager);
        ctx.put("properties", properties);
        ctx.put("encounterEventWorker", encounterEventWorker);
        ctx.put("dhisDailyOPDIPDReport", dhisDailyOPDIPDReport);
        ctx.put("dhisMonthlyEPIInfantReport", dhisMonthlyEPIInfantReport);
        ctx.put("shrWebClient", shrWebClient);
        ctx.put("conceptEventWorker", conceptEventWorker);
        ctx.put("referenceTermEventWorker", referenceTermEventWorker);
        return ctx;
    }

    @Bean(name = "dhisFacilitiesMap")
    public PropertiesFactoryBean dhisFacilitiesMap() {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("dhis_facilities.properties"));
        return propertiesFactoryBean;
    }

    private JobDetailFactoryBean jobDetail(Class jobClass, String jobName) {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(jobClass);
        jobDetailFactoryBean.setName(jobName);
        jobDetailFactoryBean.afterPropertiesSet();
        return jobDetailFactoryBean;
    }

    private CronTrigger getTrigger(String triggerName, int startDelay, String cronExpression, JobDetail jobDetail) {
        CronTriggerFactoryBean triggerFactoryBean = new CronTriggerFactoryBean();
        triggerFactoryBean.setName(triggerName);
        triggerFactoryBean.setStartDelay(startDelay);
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
        jobDetailFactoryBean.setName("dhis.daily.opdipd.post.job");
        jobDetailFactoryBean.getJobDataMap().put("reportingDate", "-1");
        jobDetailFactoryBean.afterPropertiesSet();
        return jobDetailFactoryBean.getObject();
    }

    private JobDetail dhisEPIInfantPostJob() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(DHISMonthlyEPIInfantPostJob.class);
        jobDetailFactoryBean.setName("dhis.monthly.epi.infant.post.job");
        jobDetailFactoryBean.getJobDataMap().put("reportingMonth", "-1");
        jobDetailFactoryBean.afterPropertiesSet();
        return jobDetailFactoryBean.getObject();
    }



    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }
}
