package org.sharedhealth.datasense.helpers;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@ComponentScan(basePackages = {"org.sharedhealth.datasense.config",
        "org.sharedhealth.datasense.processor",
        "org.sharedhealth.datasense.feeds.encounters",
        "org.sharedhealth.datasense.repository",
        "org.sharedhealth.datasense.client",
        "org.sharedhealth.datasense.handler",
        "org.sharedhealth.datasense.export.dhis"})
public class TestConfig {
    @Bean(name = "dhisFacilitiesMap")
    public PropertiesFactoryBean dhisFacilitiesMap() {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("dhis_facilities.properties"));
        return propertiesFactoryBean;
    }
}
