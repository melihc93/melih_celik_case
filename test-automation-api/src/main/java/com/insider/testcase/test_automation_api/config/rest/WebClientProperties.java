package com.insider.testcase.test_automation_api.config.rest;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "webclient")
@Getter
@Setter
public class WebClientProperties {
    private int connectTimeout;
    private int readTimeout;
    private int maxMemorySize;
}
