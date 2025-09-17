package com.insider.testcase.test_automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CareersPage extends BasePage {
    private final String QUALITY_ASSURANCE_PAGE = "https://useinsider.com/careers/quality-assurance";

    private final By locationsHeading = By.xpath("//*[self::h2 or self::h3][contains(normalize-space(.),'Our Locations') or contains(normalize-space(.),'Locations')]");
    private final By teamsHeadingOrLink = By.xpath("((//*[self::h2 or self::h3][contains(.,'Find your calling') or contains(.,'Teams')]) | //a[contains(normalize-space(.),'See all teams')])");
    private final By lifeHeading = By.xpath("//*[self::h2 or self::h3][contains(.,'Life at Insider') or contains(.,'Life@Insider')]");

    public CareersPage(WebDriver driver, long timeoutSec) {
        super(driver, timeoutSec);
    }

    public boolean hasLocationsTeamsLifeBlocks() {
        return !driver.findElements(locationsHeading).isEmpty()
                && !driver.findElements(teamsHeadingOrLink).isEmpty()
                && !driver.findElements(lifeHeading).isEmpty();
    }

    public QaLandingPage goToQaLanding() {
        driver.navigate().to(QUALITY_ASSURANCE_PAGE);
        return new QaLandingPage(driver, timeoutSec);
    }
}
