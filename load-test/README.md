# Load Tests (JMeter Java DSL + JUnit + GitHub Pages)

This module stress-tests **n11.com** search flows using the **JMeter Java DSL**.  
It ships with two scenarios:

1) **Smoke load test** – small, fast feedback loop (few threads × iterations)
2) **Spike load test** – ramps traffic up/down to observe system behavior under sudden load

Reports are generated as **static HTML dashboards** under `target/jmeter-report/**` and are **published to GitHub Pages** by the provided workflow.

---

## Table of contents

- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [Scenarios covered](#scenarios-covered)
- [Configuration](#configuration)
- [Running locally](#running-locally)
- [Reports & where to find them](#reports--where-to-find-them)
- [CI workflow (GitHub Actions + Pages)](#ci-workflow-github-actions--pages)
- [JMeter properties (fine-tuning reports)](#jmeter-properties-fine-tuning-reports)
- [Assertions & pass criteria](#assertions--pass-criteria)

---

## Tech stack

- **Java** 24
- **JMeter Java DSL** `us.abstracta.jmeter:jmeter-java-dsl:2.0`
- **JUnit Jupiter** (JUnit 5 API)
- **SnakeYAML** for typed YAML configuration
- **GitHub Actions** for CI and **GitHub Pages** for publishing reports

> No Lombok, no Spring — the module is intentionally small and dependency-light.

---

## Project structure

```text
load-test/
+---src
|   +---main
|   |   +---java
|   |   |   \---com
|   |   |       \---insider
|   |   |           \---testcase
|   |   |               \---load_test
|   |   |                   +---config
|   |   |                   |   |   ConfigManager.java                      # Config Loader for Injection
|   |   |                   |   |   
|   |   |                   |   \---model
|   |   |                   |           LoadTestConfig.java                 # Config Model
|   |   |                   |           
|   |   |                   +---n11
|   |   |                   |       DefaultHeader.java                      # Custom, domain dependent enum class
|   |   |                   |       
|   |   |                   \---util
|   |   |                           DurationUtil.java
|   |   |                           EnvironmentExpander.java                # Yaml to Object parser for duration, int, boolean variables
|   |   |                           
|   |   \---resources
|   |           loadtest.yaml                                               # Config file which set by overridable environment variables
|              
|   \---test
|       +---java
|       |   \---com
|       |       \---insider
|       |           \---testcase
|       |               \---load_test
|       |                   |   LoadTestBase.java                           # Main load test abstract class
|       |                   |   N11SearchPerformanceTest.java
|       |                   |   
|       |                   \---config
|       |                           JMeterPropsLoader.java                  # JMeter props loader to inject specific properties into test flow
|       |                           
|       \---resources
|               jmeter.properties                                           # Specific jmeter predefined props
|               
|
\
```

---

## Scenarios covered
### **1) Smoke:** `search_flow_smokeLoadTest`

Small thread pool & iteration count. Exercises:

- GET `/` – **home page contains** “n11”

- GET `/arama?q=iphone` – **contains** “iphone”

- GET `/arama?q=çaydanlık` – **contains** “çay”

- GET `/arama?q=iphone&pg=2` – **contains** “iphone” (page 2)

- GET `/arama?q=teknosa` – **contains** “teknosa”

### **2) Spike:** `search_iphone_spikeLoadTest`

Traffic pattern:

- **Ramp & hold** base users → **ramp & hold** spike users → **ramp down** to final users

Asserts page contains “iphone”.

### **WAF / “normal” modes**

Set in config (`mode: waf|normal`):

- normal → we fail on any HTTP sampler error and enforce p99 ≤ 5s

- waf → we expect Cloudflare blocking text (“You are unable to access”) and don’t fail on errors

---

## Configuration

Main config file: `src/main/resources/loadtest.yaml`

```yaml
base: "https://www.n11.com"
mode: ${MODE:waf}          # waf | normal

timeouts:
  connection: ${CONNECTION_TIMEOUT:10s}   # 500ms, 10s, 2m, PT10S, ...
  response:   ${RESPONSE_TIMEOUT:30s}

smokeLoadTest:
  iterations: ${SMOKE_LOAD_TEST_ITERATIONS:30}
  threads: ${SMOKE_LOAD_TEST_THREADS:3}

spikeLoadTest:
  baseUsers: ${SPIKE_LOAD_TEST_BASE_USERS:1}
  preRampTime: ${SPIKE_LOAD_TEST_RAMP_TIME:1s}
  preHoldTime: ${SPIKE_LOAD_TEST_PRE_HOLD_TIME:5s}
  spikeUsers: ${SPIKE_LOAD_TEST_SPIKE_USERS:5}
  rampUpTime: ${SPIKE_LOAD_TEST_RAMP_UP_TIME:5s}
  holdPeakTime: ${SPIKE_LOAD_TEST_HOLD_PEAK_TIME:20s}
  finalUsers: ${SPIKE_LOAD_TEST_FINAL_USERS:0}
  rampDownTime: ${SPIKE_LOAD_TEST_RAMP_DOWN_TIME:5s}
```

- Values are **environment-expandable:** `${ENV_VAR:default}`
  (`ConfigManager` uses `EnvironmentExpander` to substitute env/system values).

### Common overrides

| Setting            | Env var / Sys prop            |               Default | Notes                                         |
| ------------------ | ----------------------------- | --------------------: | --------------------------------------------- |
| Mode               | `MODE`                        |                 `waf` | `normal` → assert no errors & p99 ≤ 5s        |
| Base URL           | –                             | `https://www.n11.com` | Change it in `loadtest.yaml` or a custom file |
| Connection timeout | `CONNECTION_TIMEOUT`          |                 `10s` | e.g., `500ms`, `2s`, `PT1M`                   |
| Response timeout   | `RESPONSE_TIMEOUT`            |                 `30s` |                                               |
| Smoke iterations   | `SMOKE_LOAD_TEST_ITERATIONS`  |                  `30` |                                               |
| Smoke threads      | `SMOKE_LOAD_TEST_THREADS`     |                   `3` |                                               |
| Spike base users   | `SPIKE_LOAD_TEST_BASE_USERS`  |                   `1` |                                               |
| Spike users        | `SPIKE_LOAD_TEST_SPIKE_USERS` |                   `5` |                                               |
| Ramp/hold times    | see YAML                      |                varies | human-readable durations                      |


---

## Running locally

From the **module** directory:

```shell
  cd load-test
  mvn -B clean test
```
Override settings (env or system props):

```shell
    # "normal" mode, longer timeouts, custom smoke load
    MODE=normal \
    CONNECTION_TIMEOUT=5s \
    RESPONSE_TIMEOUT=20s \
    SMOKE_LOAD_TEST_THREADS=5 \
    SMOKE_LOAD_TEST_ITERATIONS=50 \
    mvn -B clean test
```

Optional: point to a custom JMeter properties file (see below):

```shell
    mvn -B clean test -DJMETER_PROPS=src/test/resources/jmeter.properties
```
> Java version: The module targets Java 24 in pom.xml, but runs fine on 21+. Use a Temurin JDK.

---

## Reports & where to find them
Tests produce self-contained **HTML** dashboards:
```txt
target/jmeter-report/
 ├── smoke-load-test/
 │   └── index.html  (open this)
 └── spike-load-test/
     └── index.html (open this)
```

Open `index.html` in a browser to explore throughput, latency percentiles, errors, etc.

The CI workflow also copies the latest smoke/spike dashboards into a small site with an index page and publishes it to **GitHub Pages**.

---

## CI workflow (GitHub Actions & Pages)

Workflow file: `.github/workflows/load-test.yml`

**What it does:**

1. Runs on push to master (limited paths) and on manual dispatch.

2. Sets working-directory: load-test.

3. Installs Temurin Java 24 and runs:

```txt
mvn -B clean test
```

4. Uploads the full HTML report directory as an artifact.

5. Builds a tiny **index page** (cards for **Smoke** and **Spike**) under `${{ github.workspace }}/site`, copying the latest report folders into `site/smoke/` and `site/spike/`.

6. Publishes that site via GitHub Pages (`actions/deploy-pages@v4`).

Artifacts you’ll see on each run:

- `jmeter-report` → the full `target/jmeter-report/**` contents

> The job summary prints the public Pages URL after deployment.

---

## JMeter properties (fine-tuning reports)

A minimal `src/test/resources/jmeter.properties` is included:

```properties
jmeter.reportgenerator.overall_granularity=1000
```
This makes short tests easier to read in the HTML dashboard.

#### How it’s loaded

`JMeterPropsLoader` loads properties from the classpath or a file path. If you use a custom file:

```shell
  mvn -B clean test -DJMETER_PROPS=path/to/jmeter.properties
```

---

## Assertions & pass criteria

Implemented in `LoadTestBase.assertNoErrorsIfNormal(...)`:

- normal mode

    - **0 sampler errors** (fail test otherwise)

    - **p99** ≤ 5s end-to-end sampler time

- waf mode

    - Expects Cloudflare block content; informative logs only (no failure on errors)

Per-request assertions also check expected **content substrings** depending on the mode (e.g., “n11”, “iphone”, “çay”, “teknosa” vs “You are unable to access”).