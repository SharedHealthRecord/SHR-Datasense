package org.sharedhealth.datasense.launch;

import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

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

    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public SpringLiquibase liquibase() {
        String changelogFile = environment.getProperty("DATABASE_CHANGELOG_FILE");
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog(changelogFile);
        liquibase.setIgnoreClasspathPrefix(false);
        liquibase.setDataSource(dataSource());
        liquibase.setDropFirst(false);
        liquibase.setShouldRun(true);
        return liquibase;
    }

    @Bean
    public NamedParameterJdbcTemplate namedJdbcTemplate() {
        return new NamedParameterJdbcTemplate(dataSource());
    }
}
