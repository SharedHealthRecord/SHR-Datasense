package org.sharedhealth.datasense.launch;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@EnableWebMvc
@Configuration
@ComponentScan(basePackages = {"org.sharedhealth.datasense"})
public class WebMvcConfig extends WebMvcConfigurerAdapter {
}