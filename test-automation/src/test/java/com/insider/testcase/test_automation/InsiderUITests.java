package com.insider.testcase.test_automation;

import com.insider.testcase.test_automation.pages.CareersPage;
import com.insider.testcase.test_automation.pages.HomePage;
import com.insider.testcase.test_automation.pages.LeverJobsPage;
import com.insider.testcase.test_automation.pages.QaLandingPage;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;


public class InsiderUITests extends BaseTest {

    @Test
    @Epic("Insider")
    @Feature("Home Page")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Insider home page opens successfully")
    void insiderHomePage_opensSuccessfully() {
        HomePage home = new HomePage(driver, uiConfigs.getTimeoutSec(), uiConfigs.getBaseUrl()).open();
        Assertions.assertTrue(home.isOpened());
    }

    @Test
    @Epic("Insider")
    @Feature("Careers Page")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Insider career page opens and contains locations, teams, and life")
    void insiderCareersPage_containsLocationTeamsLife() {
        HomePage home = new HomePage(driver, uiConfigs.getTimeoutSec(), uiConfigs.getBaseUrl()).open();
        Assertions.assertTrue(home.isOpened());

        CareersPage careers = home.openCareersViaCompanyMenu();
        Assertions.assertTrue(careers.hasLocationsTeamsLifeBlocks());
    }

    @Test
    @Epic("Insider")
    @Feature("Lever Jobs Page")
    @Severity(SeverityLevel.NORMAL)
    @Story("Available QA jobs page opens successfully")
    void qaLandingPage_opensSuccessfullyWithAvailableJobs() {
        HomePage home = new HomePage(driver, uiConfigs.getTimeoutSec(), uiConfigs.getBaseUrl()).open();
        Assertions.assertTrue(home.isOpened());

        CareersPage careers = home.openCareersViaCompanyMenu();
        Assertions.assertTrue(careers.hasLocationsTeamsLifeBlocks());

        QaLandingPage qa = careers.goToQaLanding();
        LeverJobsPage jobs = qa.clickSeeAllQaJobs()
                .applyFiltersViaQueryParams("Quality Assurance", "Istanbul, Turkiye");
        Assertions.assertTrue(jobs.hasAnyJobs());
    }

    @Test
    @Epic("Insider")
    @Feature("Lever Jobs Page")
    @Severity(SeverityLevel.MINOR)
    @Story("Location specific filter successfully return available jobs")
    void locationFilteredJobs_successfullyShown() {
        HomePage home = new HomePage(driver, uiConfigs.getTimeoutSec(), uiConfigs.getBaseUrl()).open();
        Assertions.assertTrue(home.isOpened());

        CareersPage careers = home.openCareersViaCompanyMenu();
        Assertions.assertTrue(careers.hasLocationsTeamsLifeBlocks());

        QaLandingPage qa = careers.goToQaLanding();
        LeverJobsPage jobs = qa.clickSeeAllQaJobs()
                .applyFiltersViaQueryParams("Quality Assurance", "Istanbul, Turkiye");
        Assertions.assertTrue(jobs.hasAnyJobs());

        Assertions.assertTrue(jobs.allCardsMatchFilters("ISTANBUL, TURKIYE", "Quality Assurance"));
    }

    @Test
    @Epic("Insider")
    @Feature("Lever Jobs Page")
    @Severity(SeverityLevel.NORMAL)
    @Story("Lever job details page opens successfully")
    void leverJobDetailsPage_opensSuccessfully() {
        HomePage home = new HomePage(driver, uiConfigs.getTimeoutSec(), uiConfigs.getBaseUrl()).open();
        Assertions.assertTrue(home.isOpened());

        CareersPage careers = home.openCareersViaCompanyMenu();
        Assertions.assertTrue(careers.hasLocationsTeamsLifeBlocks());

        QaLandingPage qa = careers.goToQaLanding();
        LeverJobsPage jobs = qa.clickSeeAllQaJobs()
                .applyFiltersViaQueryParams("Quality Assurance", "Istanbul, Turkiye");
        Assertions.assertTrue(jobs.hasAnyJobs());

        Assertions.assertTrue(jobs.allCardsMatchFilters("ISTANBUL, TURKIYE", "Quality Assurance"));

        Assertions.assertTrue(jobs.viewRoleGoesToApplication());
    }

    @Test
    @Epic("Insider")
    @Feature("Lever Jobs Page")
    @Severity(SeverityLevel.NORMAL)
    @Story("Expected fail case for check fail step screenshot")
    void failedStepScreenShoot_savedSuccessfully() {
        HomePage home = new HomePage(driver, uiConfigs.getTimeoutSec(), uiConfigs.getBaseUrl()).open();
        Assertions.assertTrue(home.isOpened());

        CareersPage careers = home.openCareersViaCompanyMenu();
        Assertions.assertTrue(careers.hasLocationsTeamsLifeBlocks());

        QaLandingPage qa = careers.goToQaLanding();
        LeverJobsPage jobs = qa.clickSeeAllQaJobs()
                .applyFiltersViaQueryParams("Quality Assurance", "tes123");
        Assertions.assertTrue(jobs.hasAnyJobs());
    }
}
