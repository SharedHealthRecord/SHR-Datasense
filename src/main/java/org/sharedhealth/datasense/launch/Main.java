package org.sharedhealth.datasense.launch;


import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.sharedhealth.datasense.client.ShrWebClient;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.export.dhis.Jobs.DHISDailyOPDIPDPostJob;
import org.sharedhealth.datasense.export.dhis.Jobs.DHISMonthlyColposcopyPostJob;
import org.sharedhealth.datasense.export.dhis.Jobs.DHISMonthlyEPIInfantPostJob;
import org.sharedhealth.datasense.export.dhis.reports.DHISDailyOPDIPDReport;
import org.sharedhealth.datasense.export.dhis.reports.DHISMonthlyColposcopyReport;
import org.sharedhealth.datasense.export.dhis.reports.DHISMonthlyEPIInfantReport;
import org.sharedhealth.datasense.feeds.encounters.EncounterEventWorker;
import org.sharedhealth.datasense.feeds.tr.ConceptEventWorker;
import org.sharedhealth.datasense.feeds.tr.DrugEventWorker;
import org.sharedhealth.datasense.feeds.tr.ReferenceTermEventWorker;
import org.sharedhealth.datasense.scheduler.jobs.CatchmentEncounterCrawlerJob;
import org.sharedhealth.datasense.scheduler.jobs.TrConceptSyncJob;
import org.sharedhealth.datasense.scheduler.jobs.TrDrugSyncJob;
import org.sharedhealth.datasense.scheduler.jobs.TrReferenceTermSyncJob;
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
import org.springframework.scheduling.annotation.EnableScheduling;
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
@EnableScheduling
@ComponentScan(basePackages = {"org.sharedhealth.datasense.config",
        "org.sharedhealth.datasense.processor",
        "org.sharedhealth.datasense.feeds",
        "org.sharedhealth.datasense.repository",
        "org.sharedhealth.datasense.client",
        "org.sharedhealth.datasense.handler",
        "org.sharedhealth.datasense.export.dhis",
        "org.sharedhealth.datasense.security",
        "org.sharedhealth.datasense.util","org.sharedhealth.datasense.scheduler.jobs"
})
public class Main {

    public static final long TEN_MINUTES = 600 * 1000L;

//    @Autowired
//    private DataSource dataSource;
//
//    @Autowired
//    private DataSourceTransactionManager txmanager;
//
//    @Autowired
//    private DatasenseProperties properties;
//
//    @Autowired
//    private EncounterEventWorker encounterEventWorker;
//
//    @Autowired
//    private DHISDailyOPDIPDReport dhisDailyOPDIPDReport;
//
//    @Autowired
//    private DHISMonthlyEPIInfantReport dhisMonthlyEPIInfantReport;
//
//    @Autowired
//    private DHISMonthlyColposcopyReport dhisMonthlyColposcopyReport;
//
//    @Autowired
//    private ShrWebClient shrWebClient;
//
//    @Autowired
//    private ConceptEventWorker conceptEventWorker;
//
//    @Autowired
//    private ReferenceTermEventWorker referenceTermEventWorker;
//
//    @Autowired
//    private DrugEventWorker drugEventWorker;

    Logger log = Logger.getLogger(Main.class);

//    private String CATCHMENT_ENCOUNTER_SYNC_JOB = "catchment.encounter.crawler.job";
//    private String CONCEPT_SYNC_JOB = "tr.concept.sync.job";
//    private String REF_TERM_SYNC_JOB = "tr.reference.term.sync.job";
//    private String DRUG_SYNC_JOB = "tr.drug.sync.job";
//
//    private String CATCHMENT_ENCOUNTER_SYNC_TRIGGER = "catchment.encounter.crawler.job.trigger";
//    private String CONCEPT_SYNC_TRIGGER = "tr.concept.sync.job.trigger";
//    private String REF_TERM_SYNC_TRIGGER = "tr.reference.term.sync.job.trigger";
//    private String DRUG_SYNC_TRIGGER = "tr.drug.sync.job.trigger";
//    private String DAILY_IPD_OPD_TRIGGER = "dhis.daily.opdipd.post.job.trigger";
//    private String MONTHLY_EPI_INFANT_TRIGGER = "dhis.monthly.epi.infant.post.job.trigger";
//    private String MONTHLY_COLPOSCOPY_TRIGGER = "dhis.monthly.colcoscopy.post.job.trigger";

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

//    @Bean
//    public SchedulerFactoryBean scheduler() {
//        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
//        schedulerFactoryBean.setDataSource(dataSource);
//        schedulerFactoryBean.setTransactionManager(txmanager);
//
//        schedulerFactoryBean.setConfigLocation(new ClassPathResource("db/quartz.properties"));
//        schedulerFactoryBean.setJobFactory(jobFactory());
//
//
//        schedulerFactoryBean.setTriggers(getTriggers());
//        schedulerFactoryBean.setApplicationContextSchedulerContextKey("applicationContext");
//        schedulerFactoryBean.setSchedulerContextAsMap(schedulerContextMap());
//        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(false);
//        try {
//            schedulerFactoryBean.afterPropertiesSet();
//        } catch (Exception e) {
//            log.error("Cannot start scheduler");
//            //:todo this should be thrown
////            throw new RuntimeException("Cannot start scheduler");
//        }
//        return schedulerFactoryBean;
//    }


    private Trigger[] getTriggers() {
        return new Trigger[]{
//                getTrigger(CATCHMENT_ENCOUNTER_SYNC_TRIGGER, 10000, "0/30 * * * * ?", jobDetail(CatchmentEncounterCrawlerJob.class, CATCHMENT_ENCOUNTER_SYNC_JOB).getObject()),
//                getTrigger(CONCEPT_SYNC_TRIGGER, 10000, "0/30 * * * * ?", jobDetail(TrConceptSyncJob.class, CONCEPT_SYNC_JOB).getObject()),
//                getTrigger(REF_TERM_SYNC_TRIGGER, 10000, "0/30 * * * * ?", jobDetail(TrReferenceTermSyncJob.class, REF_TERM_SYNC_JOB).getObject()),
//                getTrigger(DRUG_SYNC_TRIGGER, 50000, "0 0/2 * * * ?", jobDetail(TrDrugSyncJob.class, DRUG_SYNC_JOB).getObject()),
//                getTrigger(DAILY_IPD_OPD_TRIGGER, 10000, "0 0/1 * * * ?", dhisDailyOPDIPDJob()),
//                getTrigger(MONTHLY_EPI_INFANT_TRIGGER, 10000, "0 0/1 * * * ?", dhisEPIInfantPostJob()),
//                getTrigger(MONTHLY_COLPOSCOPY_TRIGGER, 10000, "0 0/1 * * * ?", dhisColposcopyPostJob())
        };
    }

//    @Bean
//    public JobFactory jobFactory() {
//        SpringBeanJobFactory springBeanJobFactory = new SpringBeanJobFactory();
//        return springBeanJobFactory;
//    }

//    @Bean
//    public Map<String, Object> schedulerContextMap() {
//        HashMap<String, Object> ctx = new HashMap<>();
//        ctx.put("txMgr", txmanager);
//        ctx.put("properties", properties);
//        ctx.put("encounterEventWorker", encounterEventWorker);
//        ctx.put("dhisDailyOPDIPDReport", dhisDailyOPDIPDReport);
//        ctx.put("dhisMonthlyEPIInfantReport", dhisMonthlyEPIInfantReport);
//        ctx.put("dhisMonthlyColposcopyReport", dhisMonthlyColposcopyReport);
//        ctx.put("shrWebClient", shrWebClient);
//        ctx.put("conceptEventWorker", conceptEventWorker);
//        ctx.put("referenceTermEventWorker", referenceTermEventWorker);
//        ctx.put("drugEventWorker", drugEventWorker);
//        return ctx;
//    }

    @Bean(name = "dhisFacilitiesMap")
    public PropertiesFactoryBean dhisFacilitiesMap() {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("dhis_facilities.properties"));
        return propertiesFactoryBean;
    }

//    private JobDetailFactoryBean jobDetail(Class jobClass, String jobName) {
//        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
//        jobDetailFactoryBean.setJobClass(jobClass);
//        jobDetailFactoryBean.setName(jobName);
//        jobDetailFactoryBean.afterPropertiesSet();
//        return jobDetailFactoryBean;
//    }
//
//    private CronTrigger getTrigger(String triggerName, int startDelay, String cronExpression, JobDetail jobDetail) {
//        CronTriggerFactoryBean triggerFactoryBean = new CronTriggerFactoryBean();
//        triggerFactoryBean.setName(triggerName);
//        triggerFactoryBean.setStartDelay(startDelay);
//        triggerFactoryBean.setCronExpression(cronExpression);
//        triggerFactoryBean.setJobDetail(jobDetail);
//        try {
//            triggerFactoryBean.afterPropertiesSet();
//        } catch (ParseException e) {
//            log.error(String.format("Error starting trigger %s", triggerName));
//            e.printStackTrace();
//        }
//        return triggerFactoryBean.getObject();
//    }
//
//    private JobDetail dhisDailyOPDIPDJob() {
//        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
//        jobDetailFactoryBean.setJobClass(DHISDailyOPDIPDPostJob.class);
//        jobDetailFactoryBean.setName("dhis.daily.opdipd.post.job");
//        jobDetailFactoryBean.getJobDataMap().put("reportingDate", "-1");
//        jobDetailFactoryBean.afterPropertiesSet();
//        return jobDetailFactoryBean.getObject();
//    }
//
//    private JobDetail dhisEPIInfantPostJob() {
//        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
//        jobDetailFactoryBean.setJobClass(DHISMonthlyEPIInfantPostJob.class);
//        jobDetailFactoryBean.setName("dhis.monthly.epi.infant.post.job");
//        jobDetailFactoryBean.getJobDataMap().put("reportingMonth", "-1");
//        jobDetailFactoryBean.afterPropertiesSet();
//        return jobDetailFactoryBean.getObject();
//    }
//
//    private JobDetail dhisColposcopyPostJob() {
//        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
//        jobDetailFactoryBean.setJobClass(DHISMonthlyColposcopyPostJob.class);
//        jobDetailFactoryBean.setName("dhis.monthly.colcoscopy.post.job.trigger");
//        jobDetailFactoryBean.getJobDataMap().put("reportingMonth", "-1");
//        jobDetailFactoryBean.afterPropertiesSet();
//        return jobDetailFactoryBean.getObject();
//    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }
}
