package org.sharedhealth.datasense.launch;

import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Autowired
    Environment environment;

    @Bean
    public DataSource getDataSource() {
        String url = environment.getProperty("DATABASE_URL");
        String user = environment.getProperty("DATABASE_USER");
        String password = environment.getProperty("DATABASE_PASSWORD");
        String driverClass = environment.getProperty("DATABASE_DRIVER");
//        int port = Integer.parseInt(environment.getProperty("DATABASE_PORT"));
        int initialPoolSize = Integer.parseInt(environment.getProperty("DATABASE_CON_POOL_SIZE"));

        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(url);
        basicDataSource.setUsername(user);
        basicDataSource.setPassword(password);
        basicDataSource.setDriverClassName(driverClass);
        basicDataSource.setInitialSize(initialPoolSize);
        return basicDataSource;
    }

    @Bean
    public DataSourceTransactionManager getTxManager() {
        return new DataSourceTransactionManager(getDataSource());
    }

    @Bean
    public SpringLiquibase liquibase() {
        String changelogFile = environment.getProperty("DATABASE_CHANGELOG_FILE");
//        String databaseSchema = environment.getProperty("DATABASE_SCHEMA");
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog(changelogFile);
//        liquibase.setDefaultSchema(databaseSchema);
        liquibase.setIgnoreClasspathPrefix(false);
        liquibase.setDataSource(getDataSource());
        liquibase.setDropFirst(false);
        liquibase.setShouldRun(true);
        return liquibase;
    }
}
