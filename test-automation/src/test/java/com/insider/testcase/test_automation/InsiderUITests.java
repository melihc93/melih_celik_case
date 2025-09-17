package com.insider.testcase.test_automation;

import com.insider.testcase.test_automation.pages.CareersPage;
import com.insider.testcase.test_automation.pages.HomePage;
import com.insider.testcase.test_automation.pages.LeverJobsPage;
import com.insider.testcase.test_automation.pages.QaLandingPage;
import org.junit.jupiter.api.*;


public class InsiderUITests extends BaseTest {

    @Test
    void insiderE2E() {
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
}
