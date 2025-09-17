package com.insider.testcase.test_automation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ui")
@Getter
@Setter
public class UIConfigs {
    private String baseUrl;
    private String browser;
    private String headless;
    private long timeoutSec;
}
