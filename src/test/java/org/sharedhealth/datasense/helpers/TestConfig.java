package org.sharedhealth.datasense.helpers;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = {"org.sharedhealth.datasense.config",
        "org.sharedhealth.datasense.processor",
        "org.sharedhealth.datasense.feeds",
        "org.sharedhealth.datasense.repository",
        "org.sharedhealth.datasense.client",
        "org.sharedhealth.datasense.handler",
        "org.sharedhealth.datasense.export.dhis",
        "org.sharedhealth.datasense.security",
        "org.sharedhealth.datasense.util",
        "org.sharedhealth.datasense.scheduler.jobs",
        "org.sharedhealth.datasense.aqs",
        "org.sharedhealth.datasense.service"})
public class TestConfig {

    @Autowired
    Environment environment;

    @Bean
    public DataSource dataSource() {
        String url = environment.getProperty("DATABASE_URL");
        String user = environment.getProperty("DATABASE_USER");
        String password = environment.getProperty("DATABASE_PASSWORD");
        String driverClass = environment.getProperty("DATABASE_DRIVER");
        int initialPoolSize = Integer.parseInt(environment.getProperty("DATABASE_CON_POOL_SIZE"));

        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(url);
        basicDataSource.setUsername(user);
        basicDataSource.setPassword(password);
        basicDataSource.setDriverClassName(driverClass);
        basicDataSource.setInitialSize(initialPoolSize);
        return basicDataSource;
    }
}
