# QA Automation Monorepo

**Three test modules** in one repo:
- **UI E2E** – Selenium + Spring Boot + JUnit 5 + Allure
- **API** – Spring Boot (WebClient) + JUnit 5 + Allure
- **Load** – JMeter Java DSL + JUnit

Each module runs in GitHub Actions, uploads artifacts (logs, reports), and publishes a **static report site on GitHub Pages**.

---

## Modules at a glance

| Module | Path | Tech | Report | CI Workflow |
|---|---|---|---|---|
| UI E2E | `test-automation/` | Java, Spring Boot, Selenium 4, JUnit 5, Allure | `target/site/allure-maven-plugin/` | `.github/workflows/ui-tests.yml` |
| API | `test-automation-api/` | Java, Spring Boot, WebClient, JUnit 5, Allure, AOP logging, Retry | `target/allure-report/` | `.github/workflows/api-test.yml` |
| Load | `load-test/` | Java, JMeter Java DSL, JUnit | `target/jmeter-report/**` (HTML dashboards) | `.github/workflows/load-test.yml` |

> Detailed READMEs live in each module:  
> • UI → [`test-automation/README.md`](test-automation/README.md)  
> • API → [`test-automation-api/README.md`](test-automation-api/README.md)  
> • Load → [`load-test/README.md`](load-test/README.md)

---

## Repo layout

```text
.
├── test-automation/           # UI E2E (Selenium + Allure)
├── test-automation-api/       # API tests (WebClient + Allure + AOP)
├── load-test/                 # Load tests (JMeter Java DSL)
└── .github/workflows/
    ├── ui-tests.yml
    ├── api-test.yml
    └── load-test.yml
```

---

## Prerequisites

- **JDK 21+** (repo uses 24 in CI; 21 works fine)

- **Maven 3.9+**

- Browsers for UI tests: **Chrome / Firefox** (Selenium Manager fetches drivers)

- Optional: Allure CLI (not required; Maven builds HTML)

---

## Quick start (local)

Run **each module** independently.

### UI E2E

```shell
    cd test-automation
    mvn -B clean test \
      -Dui.browser=chrome \
      -Dui.headless=true \
      -Dui.timeoutSec=25
    # open target/site/allure-maven-plugin/index.html
```
### API

```shell
    cd test-automation-api
    # Local profile expects application-local.yml with your API key
    SPRING_PROFILES_ACTIVE=local mvn clean verify
    # HTML: target/allure-report/index.html
```

### Load

```shell
    cd load-test
    mvn -B clean test
    # HTML dashboards under target/jmeter-report/**/index.html
```

---

## Configuration (high level)

- **UI**: `application.yml` (browser, headless, timeouts). Override via `-Dui.*` system props.

- **API**: `application.yml` + profiles

    - `application-local.yml` (ignored) → local API key

    - `application-pipeline.yml` → CI key from secret

    - Logs to `.logs/`, Allure results to `target/allure-results/`

- **Load**: `loadtest.yaml` with env placeholders (`${ENV:default}`) for timeouts, threads, spike profile.

---

## Reports

| Module | Local HTML                                                   |
| ------ | ------------------------------------------------------------ |
| UI     | `test-automation/target/site/allure-maven-plugin/index.html` |
| API    | `test-automation-api/target/allure-report/index.html`        |
| Load   | `load-test/target/jmeter-report/<name-timestamp>/index.html` |

### Artifacts (CI):

- UI: surefire reports, screenshots, allure results, Pages artifact

- API: surefire, `.logs/`, allure results & report, Pages artifact

- Load: full JMeter dashboard, Pages artifact

**GitHub Pages**: Each workflow uploads a site; the deploy job summary prints the public URL.

---

## CI/CD (GitHub Actions)

- **UI** → `.github/workflows/ui-tests.yml`
Runs Chrome & Firefox headless, builds Allure HTML, publishes to Pages.

- **API** → `.github/workflows/api-test.yml`
Sets `SPRING_PROFILES_ACTIVE=pipeline`, uses `PET_STORE_API_KEY` secret, builds Allure HTML, publishes to Pages, uploads `.logs/`.

- **Load** → `.github/workflows/load-test.yml`
Runs JMeter DSL tests, builds a tiny index that links to latest Smoke/Spike dashboards, publishes to Pages.

> All workflows upload artifacts even on failure and use concurrency groups to avoid stacked runs.

---

## Secrets & environment

- **API tests** require: `PET_STORE_API_KEY` (Repository → Settings → Secrets and variables → Actions → Secrets)

- **UI tests** has no secrets; browsers install automatically in CI.

- **Load tests** use public endpoints; tweak concurrency and timeouts via `loadtest.yaml` / env vars.

---

## Detailed Module Docs

- UI → [test-automation/README.md](test-automation/README.md)

- API → [test-automation-api/README.md](test-automation-api/README.md)

- Load → [load-test/README.md](load-test/README.md)