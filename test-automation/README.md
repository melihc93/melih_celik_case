# UI E2E Tests
**Java + Spring Boot + Selenium + JUnit 5 + Allure**

End-to-end UI tests that validate Insider’s Careers flow:

1. Open **useinsider.com**
2. Navigate **Company → Careers** and verify **Locations / Teams / Life at Insider** sections
3. Open **Quality Assurance** careers, click **See all QA jobs**
4. Filter jobs by **Team: Quality Assurance**, **Location: Istanbul, Turkiye**
5. Validate each job card (position/department/location)
6. Open **View Role** and verify navigation to the **Lever** application page

The project follows **POM (Page Object Model)**, supports **Chrome/Firefox** (parametric, headless by default), and produces **Allure** reports with **screenshots, page source, URL, and console logs on failure**. A **GitHub Actions** pipeline runs the suite and publishes the Allure HTML report to **GitHub Pages**.

---

## Table of contents
- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [How tests work](#how-tests-work)
- [Configuration](#configuration)
- [Run locally](#run-locally)
- [Allure reports](#allure-reports)
- [Screenshots on failure](#screenshots-on-failure)
- [GitHub Actions pipeline](#github-actions-pipeline)

---

## Tech stack
- **Java 24
- **Spring Boot 3.5.5** (DI & typed config via `@ConfigurationProperties`)
- **Selenium 4** (Spring parent bom supported Selenium version, Selenium Manager auto-resolves drivers)
- **JUnit 5**
- **Allure 2** (steps, attachments, HTML report)
- **GitHub Actions** (CI) + **GitHub Pages** (report hosting)

---

## Project structure

> Repository root contains `test-automation/` module; CI runs from there.
```
+---src
|   +---main
|   |   +---java
|   |   |   \---com
|   |   |       \---insider
|   |   |           \---testcase
|   |   |               \---test_automation
|   |   |                   |   TestAutomationUICaseSolutionApplication.java    # Spring-boot entrypoint
|   |   |                   |   
|   |   |                   +---config                                          # Config context
|   |   |                   |       TestConfigs.java
|   |   |                   |       UIConfigs.java
|   |   |                   |       WebDriverConfig.java
|   |   |                   |       
|   |   |                   +---pages                                           # Page Objects
|   |   |                   |       BasePage.java
|   |   |                   |       CareersPage.java
|   |   |                   |       HomePage.java
|   |   |                   |       LeverJobsPage.java
|   |   |                   |       QaLandingPage.java
|   |   |                   |       
|   |   |                   \---util
|   |   |                           UrlUtils.java
|   |   |                           
|   |   \---resources                                                           # Test Configs
|   |           application.yml
|   |           
|   \---test
|       +---java
|       |   \---com
|       |       \---insider
|       |           \---testcase
|       |               \---test_automation
|       |                   |   BaseTest.java                                   # Abstract Main Test Class
|       |                   |   InsiderUITests.java
|       |                   |   
|       |                   \---support
|       |                           ScreenShotOnFailureExtension.java           # Test Fail Interrupt Hook
|       |                           
|       \---resources
|               allure.properties                                               # Specific Allure Report Configs
```

---

## How tests work

### Flow & POM
- **BasePage** encapsulates synchronization (explicit waits), scrolling, and JS helpers.
- **HomePage → CareersPage → QaLandingPage → LeverJobsPage** encapsulate locators + actions per page.
- **Lever jobs filtering** uses the canonical Lever URL with query params:
    - `team=Quality%20Assurance`
    - `location=Istanbul,%20Turkiye`
- **Validation** reads specific card fields (title/location/categories) and **normalizes diacritics** (Türkiye/İstanbul) and also accepts **“QA”** in addition to “Quality Assurance”.

### Test class
`InsiderUITests` drives the scenario with clear assertions and Allure steps/labels.  
`BaseTest` wires Spring beans, registers the failure extension, clears cookies before each test, and quits the driver once after all tests.

---

## Configuration

`src/main/resources/application.yml`
```yaml
ui:
  baseUrl: https://useinsider.com
  browser: chrome          # override with -Dui.browser=firefox
  headless: true           # override with -Dui.headless=false
  timeoutSec: 20
  screenshotsDir: target/screenshots

test:
  screenshotsDir: target/screenshots
```

### Typed configs

`UIConfigs` binds `ui.*`
`TestConfigs` binds `test.*`
`WebDriverConfig` declares `@EnableConfigurationProperties({UIConfigs.class, TestConfigs.class}).`

### Runtime overrides (Maven system props)

```
-Dui.browser=chrome|firefox
-Dui.headless=true|false
-Dui.baseUrl=https://useinsider.com
-Dui.timeoutSec=30
```

---

## Run locally

> Ensure Chrome/Firefox are installed. Selenium Manager fetches matching drivers automatically.

#### Headless Chrome:
```bash
cd test-automation
mvn -B clean test \
-Dui.browser=chrome \
-Dui.headless=true \
-Dui.timeoutSec=25
```

---

## Allure reports

#### Dependencies (pom.xml)

```xml
<dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-junit5</artifactId>
    <scope>test</scope>
</dependency>
<plugin>
  <groupId>io.qameta.allure</groupId>
  <artifactId>allure-maven</artifactId>
  <version>2.16.0</version>
</plugin>
```

### Paths

- Raw results: target/allure-results/

- HTML: target/allure-report/

### Generate HTML

> mvn test/verify goals automatically generate HTML allure reports at target/allure-report

---

## Screenshots on failure

Implemented via `ScreenShotOnFailureExtension` (JUnit 5 `BeforeTestExecutionCallback` and `TestWatcher`):

Resolves `WebDriver` & screenshot directory via Suppliers (no early NPE).

Attaches artifacts to Allure while the test is still “open”. Allure report integration is TODO.

Also saves PNGs to disk: `target/screenshots/`.

---

## GitHub Actions pipeline

Workflow: .github/workflows/ui-tests.yml

#### What it does

- Triggers on push to master (limited paths), PRs, and manual runs.

- Runs the suite in test-automation/ on ubuntu-latest.

- Installs JDK + Chrome + Firefox, runs tests headless.

- Builds Allure HTML, uploads artifacts (JUnit, screenshots, allure-results).

- Publishes Allure HTML to GitHub Pages and prints the Page URL in the job summary.

#### Key steps (excerpt)

```yaml
jobs:
ui-e2e:
runs-on: ubuntu-latest
defaults:
run:
working-directory: test-automation
steps:
- uses: actions/checkout@v4
- uses: actions/setup-java@v4
with: { distribution: temurin, java-version: '21', cache: maven }
- uses: browser-actions/setup-chrome@v1
- uses: browser-actions/setup-firefox@v1
- run: mvn -B clean verify -Dmaven.test.failure.ignore=true
- run: mvn -B -DskipTests allure:report
- uses: actions/upload-pages-artifact@v3
with: { path: target/site/allure-maven-plugin }

deploy-pages:
needs: ui-e2e
runs-on: ubuntu-latest
environment:
name: github-pages
url: ${{ steps.deployment.outputs.page_url }}
steps:
- id: deployment
uses: actions/deploy-pages@v4
- name: Add link to job summary
run: |
echo "### ✅ Allure report" >> $GITHUB_STEP_SUMMARY
echo "${{ steps.deployment.outputs.page_url }}" >> $GITHUB_STEP_SUMMARY
```

Artifacts uploaded on every run (even on failure):

- `target/surefire-reports/**`

- `target/screenshots/**`

- `target/allure-results/**`

- Pages artifact: `target/site/allure-maven-plugin/**` (published to GitHub Pages)