package com.insider.testcase.test_automation.config;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({UIConfigs.class, TestConfigs.class})
@RequiredArgsConstructor
public class WebDriverConfig {
    private final UIConfigs configs;

    @Bean(destroyMethod = "quit")
    @Scope("singleton")
    public WebDriver webDriver() {
        WebDriver driver;
        switch (configs.getBrowser().toLowerCase()) {
            case "firefox" -> {
                FirefoxOptions fo = new FirefoxOptions();
                if (Boolean.parseBoolean(configs.getHeadless())) fo.addArguments("-headless");
                fo.addArguments("--width=1600", "--height=1000");
                driver = new FirefoxDriver(fo);
            }
            case "chrome" -> {
                ChromeOptions co = new ChromeOptions();
                if (Boolean.parseBoolean(configs.getHeadless())) co.addArguments("--headless=new");
                co.addArguments("--window-size=1600,1000", "--disable-gpu",
                        "--no-sandbox", "--disable-dev-shm-usage");
                driver = new ChromeDriver(co);
            }
            default -> throw new IllegalArgumentException("Unsupported browser: " + configs.getBrowser());
        }

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(configs.getTimeoutSec()));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(configs.getTimeoutSec()));
        driver.manage().deleteAllCookies();
        return driver;
    }
}
