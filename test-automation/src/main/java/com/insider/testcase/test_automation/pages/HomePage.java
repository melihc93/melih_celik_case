package com.insider.testcase.test_automation.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class HomePage extends BasePage {
    private final String baseUrl;
    private final By companyNav = By.xpath("//nav//a[normalize-space(.)='Company' or contains(.,'Company')]");
    private final By careersLinkInDropdown = By.xpath("//a[contains(@href,'/careers') and (normalize-space(.)='Careers' or contains(.,'Career'))]");

    public HomePage(WebDriver driver, long timeoutSec, String baseUrl) {
        super(driver, timeoutSec);
        this.baseUrl = baseUrl;
    }

    public HomePage open() {
        driver.navigate().to(baseUrl + "/");
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(companyNav),
                ExpectedConditions.titleContains("Insider")
        ));
        return this;
    }

    public boolean isOpened() {
        return driver.getTitle().toLowerCase().contains("insider")
                || isPresent(companyNav);
    }

    public CareersPage openCareersViaCompanyMenu() {
        WebElement company = waitVisible(companyNav);
        hover(company);
        WebElement careers = waitVisible(careersLinkInDropdown);
        jsClick(careers);
        return new CareersPage(driver, timeoutSec);
    }
}
