package com.insider.testcase.test_automation.pages;

import org.openqa.selenium.*;

import static com.insider.testcase.test_automation.util.UrlUtils.encode;

public class LeverJobsPage extends BasePage {
    private final String LEVER_USEINSIDER_BASE_URL = "https://jobs.lever.co/useinsider";
    private final By postings  = By.cssSelector("a.posting, div.posting");
    private final By noResults = By.xpath("//*[contains(.,'No results') or contains(.,'0 result')]");
    private final By firstPosting = By.xpath("(//a[contains(@class,'posting') and @href])[1]");

    public LeverJobsPage(WebDriver driver, long timeoutSec) { super(driver, timeoutSec); }

    public LeverJobsPage applyFiltersViaQueryParams(String team, String loc) {
        String base = driver.getCurrentUrl();
        if (!base.startsWith(LEVER_USEINSIDER_BASE_URL)) {
            base = LEVER_USEINSIDER_BASE_URL;
        }
        String filtered = base + "?team=" + encode(team) + "&location=" + encode(loc);
        driver.navigate().to(filtered);

        wait.until(d -> !d.findElements(postings).isEmpty() || !d.findElements(noResults).isEmpty());
        return this;
    }

    public boolean hasAnyJobs() {
        return !driver.findElements(postings).isEmpty();
    }

    public boolean allCardsMatchFilters(String locationText, String roleText) {
        var cards = driver.findElements(postings);
        if (cards.isEmpty()) return false;

        for (var card : cards) {
            String text = card.getText();
            if (!(text.contains(locationText) && text.contains(roleText))) return false;
        }
        return true;
    }

    public boolean viewRoleGoesToApplication() {
        var cards = driver.findElements(postings);
        if (cards.isEmpty()) return false;
        click(firstPosting);

        String main = driver.getWindowHandle();

        for (String h : driver.getWindowHandles()) {
            if (!h.equals(main)) { driver.switchTo().window(h); break; }
        }
        String url = driver.getCurrentUrl().toLowerCase();
        return url.contains(LEVER_USEINSIDER_BASE_URL);
    }


}
