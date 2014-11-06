package org.sharedhealth.datasense.helpers;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"org.sharedhealth.datasense.config",
        "org.sharedhealth.datasense.processors",
        "org.sharedhealth.datasense.feeds.encounters",
        "org.sharedhealth.datasense.repository",
        "org.sharedhealth.datasense.client"})
public class TestConfig {
}
