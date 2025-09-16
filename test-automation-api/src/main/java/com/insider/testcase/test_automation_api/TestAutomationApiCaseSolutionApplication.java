package com.insider.testcase.test_automation_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class TestAutomationApiCaseSolutionApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestAutomationApiCaseSolutionApplication.class, args);
	}

}
