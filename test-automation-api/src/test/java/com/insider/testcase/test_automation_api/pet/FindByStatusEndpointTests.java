package com.insider.testcase.test_automation_api.pet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insider.testcase.test_automation_api.TestAutomationApiCaseSolutionApplication;
import com.insider.testcase.test_automation_api.pet.client.PetClient;
import com.insider.testcase.test_automation_api.pet.client.response.Pet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestConstructor;

import java.util.List;

import static com.insider.testcase.test_automation_api.pet.util.TestResiliences.assertWithRetry;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(classes = TestAutomationApiCaseSolutionApplication.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class FindByStatusEndpointTests {
    @Autowired
    private PetClient petClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${test-resilience.rest.pet.find-by-status.delay}")
    private int findByStatusEndpointDelay;
    @Value("${test-resilience.rest.pet.find-by-status.attempt}")
    private int findByStatusEndpointRetryAttempt;

    @Test
    public void findByStatus_successfullyReturn() throws Throwable {
        ResponseEntity<?> findPetsByStatusResponse = petClient.findByStatus(List.of("asd"));
        List<Pet> foundPets = objectMapper.convertValue(findPetsByStatusResponse.getBody(), new TypeReference<>() {
        });

        assertAll(
                () -> assertWithRetry(
                        () ->  assertThat(findPetsByStatusResponse.getStatusCode().is2xxSuccessful()).isTrue(),
                        findByStatusEndpointRetryAttempt,
                        java.time.Duration.ofMillis(findByStatusEndpointDelay),
                        AssertionError.class
                ),
                () -> assertWithRetry(
                        () -> assertThat(foundPets.size()).isNotZero(),
                        findByStatusEndpointRetryAttempt,
                        java.time.Duration.ofMillis(findByStatusEndpointDelay),
                        AssertionError.class
                )
        );
    }
}
