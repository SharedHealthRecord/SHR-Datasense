package org.sharedhealth.datasense.launch;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
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
        String shr = environment.getProperty("DATABASE_SCHEMA");
        int port = Integer.parseInt(environment.getProperty("DATABASE_PORT"));

        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl(url);
        dataSource.setUser(user);
        dataSource.setPassword(password);
        dataSource.setDatabaseName(shr);
        dataSource.setPortNumber(port);
        return dataSource;
    }

    @Bean
    public DataSourceTransactionManager getTxManager() {
        return new DataSourceTransactionManager(getDataSource());
    }
}
