package com.insider.testcase.test_automation;

import com.insider.testcase.test_automation.config.TestConfigs;
import com.insider.testcase.test_automation.config.UIConfigs;
import com.insider.testcase.test_automation.config.WebDriverConfig;
import com.insider.testcase.test_automation.support.ScreenShotOnFailureExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {WebDriverConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {
    @Autowired
    protected WebDriver driver;
    @Autowired
    protected UIConfigs uiConfigs;
    @Autowired
    protected TestConfigs testConfigs;

    @RegisterExtension
    ScreenShotOnFailureExtension screenshotWatcher = new ScreenShotOnFailureExtension(() -> driver, () -> testConfigs.getScreenshotsDir());

    @BeforeEach
    void clearState() {
        driver.manage().deleteAllCookies();
    }

    @AfterAll
    void teardown() {
        if (driver != null) driver.quit();
    }
}
