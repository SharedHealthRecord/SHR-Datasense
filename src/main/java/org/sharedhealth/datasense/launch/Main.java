package org.sharedhealth.datasense.launch;


import org.apache.log4j.Logger;
import org.quartz.spi.JobFactory;
import org.sharedhealth.datasense.export.dhis.reports.DHISDailyOPDIPDReport;
import org.sharedhealth.datasense.export.dhis.reports.DHISMonthlyColposcopyReport;
import org.sharedhealth.datasense.export.dhis.reports.DHISMonthlyEPIInfantReport;
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
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.valueOf;
import static java.lang.System.getenv;

@Configuration
@Import({DatabaseConfig.class, ScheduleConfig.class, ApplicationConfig.class})
@ComponentScan(basePackages = {"org.sharedhealth.datasense.config",
        "org.sharedhealth.datasense.processor",
        "org.sharedhealth.datasense.feeds",
        "org.sharedhealth.datasense.repository",
        "org.sharedhealth.datasense.client",
        "org.sharedhealth.datasense.handler",
        "org.sharedhealth.datasense.export.dhis",
        "org.sharedhealth.datasense.security",
        "org.sharedhealth.datasense.util", "org.sharedhealth.datasense.scheduler.jobs"
})
public class Main {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private DataSourceTransactionManager txmanager;

    @Autowired
    private DHISDailyOPDIPDReport dhisDailyOPDIPDReport;

    @Autowired
    private DHISMonthlyEPIInfantReport dhisMonthlyEPIInfantReport;

    @Autowired
    private DHISMonthlyColposcopyReport dhisMonthlyColposcopyReport;

    Logger log = Logger.getLogger(Main.class);

    @Bean
    public EmbeddedServletContainerFactory getFactory() {
        Map<String, String> env = getenv();
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        factory.addInitializers(new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
                ServletRegistration.Dynamic shr = servletContext.addServlet("datasense", DispatcherServlet.class);
                shr.addMapping("/");
                shr.setInitParameter("contextClass", "org.springframework.web.context.support" +
                        ".AnnotationConfigWebApplicationContext");
                shr.setInitParameter("contextConfigLocation", "org.sharedhealth.datasense.launch.WebMvcConfig");
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


//        schedulerFactoryBean.setApplicationContextSchedulerContextKey("applicationContext");
        schedulerFactoryBean.setSchedulerContextAsMap(schedulerContextMap());
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(false);
        try {
            schedulerFactoryBean.afterPropertiesSet();
        } catch (Exception e) {
            log.error("Cannot start scheduler");
            //:todo this should be thrown
            throw new RuntimeException("Cannot start scheduler:-", e);
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
        HashMap<String, Object> ctx = new HashMap<>();
        ctx.put("dhisDailyOPDIPDReport", dhisDailyOPDIPDReport);
        ctx.put("dhisMonthlyEPIInfantReport", dhisMonthlyEPIInfantReport);
        ctx.put("dhisMonthlyColposcopyReport", dhisMonthlyColposcopyReport);
        return ctx;
    }

    @Bean(name = "dhisFacilitiesMap")
    public PropertiesFactoryBean dhisFacilitiesMap() {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("dhis_facilities.properties"));
        return propertiesFactoryBean;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }
}
