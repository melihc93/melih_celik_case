package com.insider.testcase.test_automation_api.pet.client.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "client.pet")
@Getter
@Setter
public class PetClientProperties {
    private String baseUrl;
    private String findByStatus;
}
