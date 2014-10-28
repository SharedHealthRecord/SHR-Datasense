package org.sharedhealth.datasense.launch;


import liquibase.integration.spring.SpringLiquibase;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.sql.DataSource;
import java.io.InputStream;
import java.util.Map;

import static java.lang.Integer.valueOf;
import static java.lang.System.getenv;

@Configuration
@Import({DatabaseConfig.class})
public class Main {

    @Autowired
    DataSource dataSource;

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
    public SpringLiquibase liquibase() {
        Map<String, String> env = getenv();
        String changelogFile = env.get("DATABASE_CHANGELOG_FILE");
        String databaseSchema = env.get("DATABASE_SCHEMA");
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog(changelogFile);
        liquibase.setDefaultSchema(databaseSchema);
        liquibase.setIgnoreClasspathPrefix(false);
        liquibase.setDataSource(dataSource);
        liquibase.setDropFirst(false);
        liquibase.setShouldRun(true);
        return liquibase;
    }



    @Bean
    public StdSchedulerFactory scheduler() {
        StdSchedulerFactory factory = new StdSchedulerFactory();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("db/quartz.properties");
        try {
            factory.initialize(inputStream);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return factory;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
        System.out.println("Success");
    }
}
