package org.sharedhealth.datasense.launch;


import org.quartz.spi.JobFactory;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.encounters.EncounterEventWorker;
import org.sharedhealth.datasense.scheduler.jobs.CatchmentEncounterCrawlerJob;
import org.springframework.beans.factory.annotation.Autowired;
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
        "org.sharedhealth.datasense.processors",
        "org.sharedhealth.datasense.feeds.encounters",
        "org.sharedhealth.datasense.repository",
        "org.sharedhealth.datasense.client"})
public class Main {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DataSourceTransactionManager txmanager;

    @Autowired
    private DatasenseProperties properties;

    @Autowired
    private EncounterEventWorker encounterEventWorker;

    @Bean
    public EmbeddedServletContainerFactory getFactory() {
        Map<String, String> env = getenv();
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        factory.addInitializers(new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
                ServletRegistration.Dynamic shr = servletContext.addServlet("shr", DispatcherServlet.class);
                shr.addMapping("/");
                shr.setInitParameter("contextClass", "org.springframework.web.context.support.AnnotationConfigWebApplicationContext");
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
        schedulerFactoryBean.setTriggers(catchmentEncounterJobTrigger().getObject());
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

    @Bean
    public JobFactory jobFactory() {
        SpringBeanJobFactory springBeanJobFactory = new SpringBeanJobFactory();
        return springBeanJobFactory;
    }

    @Bean
    public Map<String, Object> schedulerContextMap() {
        HashMap<String, Object> ctx = new HashMap<String, Object>();
        ctx.put("txMgr", txmanager);
        ctx.put("properties", properties);
        ctx.put("encounterEventWorker", encounterEventWorker);
        return ctx;
    }

    @Bean
    public JobDetailFactoryBean catchmentEncounterJob() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(CatchmentEncounterCrawlerJob.class);
        jobDetailFactoryBean.setName("catchment.encounter.crawler.job");
        jobDetailFactoryBean.afterPropertiesSet();
        return jobDetailFactoryBean;
    }

    @Bean
    public CronTriggerFactoryBean catchmentEncounterJobTrigger() {
        CronTriggerFactoryBean triggerFactoryBean = new CronTriggerFactoryBean();
        triggerFactoryBean.setName("catchment.encounter.crawler.job.trigger");
        triggerFactoryBean.setStartDelay(10000);
        triggerFactoryBean.setCronExpression("0/30 * * * * ?");
        triggerFactoryBean.setJobDetail(catchmentEncounterJob().getObject());
        try {
            triggerFactoryBean.afterPropertiesSet();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return triggerFactoryBean;
    }


    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }
}
