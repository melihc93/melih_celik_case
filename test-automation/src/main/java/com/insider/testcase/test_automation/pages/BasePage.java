package com.insider.testcase.test_automation.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final Actions actions;
    protected final long timeoutSec;

    protected BasePage(WebDriver driver, long timeoutSec) {
        this.driver = driver;
        this.timeoutSec = timeoutSec;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));
        this.actions = new Actions(driver);
    }

    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    protected void hover(WebElement el) {
        actions.moveToElement(el).pause(Duration.ofMillis(150)).perform();
    }

    protected boolean isPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected void maybeAcceptCookies() {
        By[] candidates = new By[] {
                By.xpath("//button[contains(.,'Accept') or contains(.,'I agree') or contains(.,'Kabul') or contains(.,'Kabul Et')]"),
                By.cssSelector("button#onetrust-accept-btn-handler"),
                By.xpath("//div[contains(@class,'cookie')]//button")
        };
        for (By c : candidates) {
            try {
                WebElement b = wait.withTimeout(Duration.ofSeconds(3))
                        .until(ExpectedConditions.elementToBeClickable(c));
                b.click();
                break;
            } catch (Exception ignored) {}
        }
    }
}
