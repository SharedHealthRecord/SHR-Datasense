package org.sharedhealth.datasense.launch;

import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class QuartzConfig {

    @Autowired
    DataSource dataSource;

    @Autowired
    PlatformTransactionManager platformTransactionManager;

    @Bean
    public SchedulerFactoryBean scheduler() {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setDataSource(dataSource);
        schedulerFactoryBean.setTransactionManager(platformTransactionManager);

        schedulerFactoryBean.setConfigLocation(new ClassPathResource("db/quartz.properties"));
        schedulerFactoryBean.setJobFactory(jobFactory());
        schedulerFactoryBean.setApplicationContextSchedulerContextKey("applicationContext");
//        schedulerFactoryBean.setSchedulerContextAsMap(Map().asJava)
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);

        return schedulerFactoryBean;
    }

    @Bean
    public JobFactory jobFactory() {
        return new SpringBeanJobFactory();
    }
}
