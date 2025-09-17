package com.insider.testcase.test_automation.pages;

import org.openqa.selenium.*;

public class QaLandingPage extends BasePage {
    private final String LEVER_USEINSIDER_BASE_URL = "https://jobs.lever.co/useinsider";
    private final By seeAllQaJobs = By.xpath("//a[.//span[normalize-space(.)='See all QA jobs'] or normalize-space(.)='See all QA jobs']");

    public QaLandingPage(WebDriver driver, long timeoutSec) {
        super(driver, timeoutSec);
    }

    public LeverJobsPage clickSeeAllQaJobs() {
        WebElement btn = waitVisible(seeAllQaJobs);
        jsClick(btn);

        boolean switched = false;
        for (String h : driver.getWindowHandles()) {
            driver.switchTo().window(h);
            if (driver.getCurrentUrl().contains(LEVER_USEINSIDER_BASE_URL)) {
                switched = true; break;
            }
        }
        if (!switched) {
            driver.navigate().to(LEVER_USEINSIDER_BASE_URL);
        }
        return new LeverJobsPage(driver, timeoutSec);
    }
}
