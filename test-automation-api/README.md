# API Automation Tests
**Spring Boot + WebClient + JUnit 5 + Allure + GitHub Actions**

This module (`test-automation-api/`) contains API tests against the **Swagger Petstore** endpoints.  
It uses a **typed, reusable REST client** (Spring WebClient), **AOP logging**, **resilience (retry)** for flaky reads, **data generation** to avoid collisions, and **Allure** for rich test reporting.  
A GitHub Actions pipeline runs the suite and **publishes the Allure HTML report to GitHub Pages**; raw results, surefire reports, and logs are uploaded as artifacts.

---

## Table of contents

- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [How it works](#how-it-works)
    - [RestClient & WebClient](#restclient--webclient)
    - [PetClient (endpoints)](#petclient-endpoints)
    - [PetService](#petservice)
    - [AOP logging](#aop-logging)
    - [Resilience: retries](#resilience-retries)
    - [Randomized data](#randomized-data)
- [Configuration](#configuration)
    - [application.yml (defaults)](#applicationyml-defaults)
    - [Profiles](#profiles)
    - [Allure configuration](#allure-configuration)
    - [Logging](#logging)
- [Run locally](#run-locally)
- [Allure report locations](#allure-report-locations)
- [GitHub Actions pipeline](#github-actions-pipeline)

---

## Tech stack

- **Java 24** (works with 21+)
- **Spring Boot 3.5**
- **Spring Web + WebFlux (WebClient)**
- **Spring AOP** (AspectJ annotations, woven by Spring)
- **Spring Retry** (annotation-driven retries)
- **JUnit 5** (parameterized & standard tests)
- **Allure 2** (JUnit 5 integration + Maven report)
- **GitHub Actions & Pages** (CI + report hosting)

---

## Project structure

```text
+---.logs
|       .log
+---src
|   +---main
|   |   +---java
|   |   |   \---com
|   |   |       \---insider
|   |   |           \---testcase
|   |   |               \---test_automation_api
|   |   |                   |   TestAutomationApiCaseSolutionApplication.java               # Spring boot entry
|   |   |                   |   
|   |   |                   +---config
|   |   |                   |   \---rest
|   |   |                   |           RestClient.java                                     # My custom rest client with predefined timeouts, and max-response-size configs
|   |   |                   |           WebClientConfig.java                                # WebClient bean 
|   |   |                   |           WebClientProperties.java                            # WebClient configs model
|   |   |                   |           
|   |   |                   +---pet
|   |   |                   |   +---client
|   |   |                   |   |   |   PetClient.java                                      # Pet Domain clients
|   |   |                   |   |   |   
|   |   |                   |   |   +---config
|   |   |                   |   |   |       PetClientProperties.java                        # Endpoint and base urls for pet domain client
|   |   |                   |   |   |       
|   |   |                   |   |   +---exception
|   |   |                   |   |   |       EmptyResponseException.java                     # Custom pet client exception
|   |   |                   |   |   |       NotFoundResponseException.java.java             # Custom pet client exception
|   |   |                   |   |   |       
|   |   |                   |   |   +---logging
|   |   |                   |   |   |       PetClientLoggingAspect.java                     # Aspect logger for pet client
|   |   |                   |   |   |       
|   |   |                   |   |   +---request
|   |   |                   |   |   |       AddNewPetInvalidBody.java
|   |   |                   |   |   |       AddNewPetRequest.java
|   |   |                   |   |   |       QueryParamNames.java
|   |   |                   |   |   |       UpdateExistingPetInvalidIdRequest.java
|   |   |                   |   |   |       UpdateExistingPetRequest.java
|   |   |                   |   |   |       
|   |   |                   |   |   \---response
|   |   |                   |   |           DeleteByIdResponse.java
|   |   |                   |   |           Pet.java
|   |   |                   |   |           UploadImageResponse.java
|   |   |                   |   |           ErrorResponse.java
|   |   |                   |   |           
|   |   |                   |   +---model
|   |   |                   |   |       Category.java
|   |   |                   |   |       Status.java
|   |   |                   |   |       Tag.java
|   |   |                   |   |       
|   |   |                   |   \---service
|   |   |                   |           PetService.java                                     # Pet domain related class
|   |   |                   |           
|   |   |                   \---util
|   |   |                           RandomValueGenerator.java
|   |   |                           
|   |   \---resources
|   |           application-local.yml                                                       # local config file, which is gitignored for security purposes
|   |           application-pipeline.yml                                                    # pipeline config file
|   |           application.yml                                                             # main config file
|   |           
|   \---test
|       +---java
|       |   \---com
|       |       \---insider
|       |           \---testcase
|       |               \---test_automation_api
|       |                   \---pet
|       |                       |   AddNewPetEndpointTests.java
|       |                       |   DeletesAPetEndpointTests.java
|       |                       |   FindByStatusEndpointTests.java
|       |                       |   FindPetByIdEndpointTests.java
|       |                       |   UpdateExistingPetEndpointTests.java
|       |                       |   UpdatePetInTheStoreWithFormDataEndpointTests.java
|       |                       |   UploadImageEndpointTests.java
|       |                       |   
|       |                       \---util
|       |                               TestResiliences.java                                # For flaky safe test executions
|       |                               
|       \---resources
|               allure.properties                                                           # Auto injected allure property file
|               cute-cat.png
|               logback-test.xml                                                            # Log profile
\            
```

---

## How it works

### RestClient & WebClient

- `WebClientConfig` builds a **Reactor Netty** client with connect/read timeouts and max **in-memory** size from `webclient.*` properties.

- `RestClient` is a small wrapper exposing `exchange(...)` overloads:

    - sets content type to JSON if missing

    - accepts optional `HttpHeaders`

    - returns `ResponseEntity<T>` via `exchangeToMono(...).block()` for a simple, sync test style.

### PetClient (endpoints)

- Encapsulates Petstore operations using `PetClientProperties` (baseUrl + paths) and the `RestClient`:

    - `findByStatus(List<String>)`

    - `addNewPet(body)`

    - `findPetById(id)` (with **retry** for empty body)

    - `updateExistingPet(body)`

    - `uploadImage(petId, meta, file, contentType)` (**multipart**)

    - `updatePetWithFormData(petId, name, status)` (**form-url-encoded**)

    - `deletePetById(petId)` (api_key **header**)

- Uses `ParameterizedTypeReference` to keep responses strongly typed.

### PetService

Injected `PetClient` to `PetService` to perform pet domain related things here, like:

- `isPetExists(Long petId)`: sending proper endpoint to GET request to check whether pet is previously defined or not, in order to avoid **test flakes**
- `generateNotExistedPetId()`: generating not existed pet id to avoid test data's affect other tester data's, it is also for **test flakes**

### AOP logging

- `PetClientLoggingAspect` logs method name, args preview, elapsed time, **HTTP status**, headers (with **sensitive headers redacted**) and body (truncated).

- Level is **INFO/WARN** for summary; **DEBUG** prints headers/body.
Enable DEBUG for the aspect via `logging.level....PetClientLoggingAspect: DEBUG`.

### Resilience: retries

- `findPetById`:

    - Throws `EmptyResponseException` if body is null (rare, flaky scenario).
    - `@Retryable` retries on that exception with attempt/delay configured under:
      ```yaml
      test-resilience:
        rest:
          pet:
            find-by-status:
              attempt: <>
              delay: <>
      ```
    - `@Recover` returns the last response after retries.
- `updatePetWithFormData`:

    - Throws `NotFoundResponseException` if response is 404, even i created pet previously (rare, flaky scenario).
    - `@Retryable` retries on that exception with attempt/delay configured under:
      ```yaml
      test-resilience:
        rest:
          pet:
            update-with-form:
              attempt: <>
              delay: <>
      ```
    - `@Recover` returns the last response after retries.
  
- `TestResiliences.assertWithRetry(...)` allows re-running fragile assertions a few times with a delay (defaults via system props `test.retry.*`).

### Randomized data

- `RandomValueGenerator` creates unique IDs / names and photo URLs so tests are idempotent and avoid data collisions across runs:

    - `randomLong()`, `randomString()`, `randomPhotoUrls()`

---

## Configuration

### `application.yml` (**defaults**)

```yaml
spring:
  application:
    name: Test Automation API Case Solution

webclient:
  connect-timeout: 5000   # ms
  read-timeout: 10000     # ms
  max-memory-size: 512    # MB

client:
  pet:
    findByStatus: /v2/pet/findByStatus
    addNewPet: /v2/pet
    findPetById: /v2/pet/%s
    uploadAnImage: /v2/pet/%s/uploadImage
    updateExistingPet: /v2/pet
    updatePetInStoreWithFormData: /v2/pet/%s
    deleteAPet: /v2/pet/%s

test-resilience:
  rest:
    pet:
      find-by-status:
        delay: 500       # ms backoff
        attempt: 10      # max attempts

logging:
  file:
    name: .logs/test-logs.log
  level:
    com.insider.testcase.test_automation_api.pet.client.logging.PetClientLoggingAspect: DEBUG
```

### Profiles
- **local** (`application-local.yml` – not committed):
  ```yaml
  client:
  pet:
    baseUrl: https://petstore.swagger.io
    apiKey: <API_KEY>

  ```
- pipeline (`application-pipeline.yml` - used in CI):
  ```yaml
  client:
  pet:
    baseUrl: https://petstore.swagger.io
    apiKey: ${PET_STORE_API_KEY}

  ```
Activate with `SPRING_PROFILES_ACTIVE`=local|pipeline.
The pipeline job sets `SPRING_PROFILES_ACTIVE`=pipeline.

### Allure configuration
- `allure.properties`:
    ```properties
    allure.results.directory=target/allure-results
    ```
- Maven plugin (`pom.xml`) generates HTML to `target/allure-report/` during verify phase.
### Logging
- File logs go to `.logs/` (see `application.yml` and `logback-spring.xml`).

- CI uploads the `.logs` directory as the `app-logs` artifact.

---

## Run locally
From the module:

```shell
    cd test-automation-api
    # Local profile (uses your local API key)
    SPRING_PROFILES_ACTIVE=local \
    mvn clean verify
```

---

## Allure report locations
- **Raw**: `target/allure-results/**`

- **HTML**: `target/allure-report/index.html` (generated by `allure-maven` at the end of `verify goal`)

Open it locally after the run:
```shell
    open target/allure-report/index.html  # macOS
    xdg-open target/allure-report/index.html  # Linux
```

---

## GitHub Actions pipeline

Workflow: `.github/workflows/api-test.yml`

**Key steps**:

1. Triggers on pushes to `master` (limited to this module/workflow), PRs, and manual dispatch.

2. Sets working directory to `test-automation-api/`.

3. Sets `SPRING_PROFILES_ACTIVE=pipeline` so API key is read from secrets.

4. Runs:

   ```shell
   mvn -U -B -ntp clean verify -Dmaven.test.failure.ignore=true
   ```
    The Allure HTML is created during verify.

5. Uploads artifacts:

   - `allure-results` → `target/allure-results/**`

   - `allure-report` → `target/allure-report/**`

   - `surefire-reports` → `target/surefire-reports/**`

   - `app-logs` → `.logs/**`

6. Publishes **Allure HTML** to **GitHub Pages** using `actions/deploy-pages@v4` and prints the link in the job summary.

> The concurrency group `ci-${{ github.ref }}` avoids stacking runs on the same ref.