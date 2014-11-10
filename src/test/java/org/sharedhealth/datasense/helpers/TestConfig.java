package org.sharedhealth.datasense.helpers;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"org.sharedhealth.datasense.config",
        "org.sharedhealth.datasense.processor",
        "org.sharedhealth.datasense.feeds.encounters",
        "org.sharedhealth.datasense.repository",
        "org.sharedhealth.datasense.client",
        "org.sharedhealth.datasense.handler"})
public class TestConfig {
}
