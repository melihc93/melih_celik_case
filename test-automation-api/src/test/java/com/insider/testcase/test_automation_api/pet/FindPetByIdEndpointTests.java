package com.insider.testcase.test_automation_api.pet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insider.testcase.test_automation_api.TestAutomationApiCaseSolutionApplication;
import com.insider.testcase.test_automation_api.pet.client.PetClient;
import com.insider.testcase.test_automation_api.pet.model.Category;
import com.insider.testcase.test_automation_api.pet.model.Tag;
import com.insider.testcase.test_automation_api.pet.client.request.AddNewPetRequest;
import com.insider.testcase.test_automation_api.pet.client.response.Pet;
import com.insider.testcase.test_automation_api.pet.service.PetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestConstructor;

import java.util.List;

import static com.insider.testcase.test_automation_api.util.RandomValueGenerator.*;
import static com.insider.testcase.test_automation_api.util.RandomValueGenerator.randomLong;
import static com.insider.testcase.test_automation_api.util.RandomValueGenerator.randomString;
import static com.insider.testcase.test_automation_api.pet.util.TestResiliences.assertWithRetry;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(classes = TestAutomationApiCaseSolutionApplication.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class FindPetByIdEndpointTests {
    @Autowired
    private PetClient petClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PetService petService;
    @Value("${test-resilience.rest.pet.find-by-status.delay}")
    private int findByStatusEndpointDelay;
    @Value("${test-resilience.rest.pet.find-by-status.attempt}")
    private int findByStatusEndpointRetryAttempt;

    @Test
    public void alreadyCreatedPet_returnFromByIdSearchSuccessfully() throws Throwable {
        Long generatedRandomPetId = petService.generateNotExistedPetId();
        Long generatedRandomCategoryId = randomLong();
        String generatedRandomCategoryName = randomString();
        String generatedRandomPetName = randomString();
        List<String> randomPhotoUrls = randomPhotoUrls();
        Long generatedRandomTagId = randomLong();
        String generatedRandomTagName = randomString();

        ResponseEntity<?> addNewPetResponse = petClient.addNewPet(AddNewPetRequest.builder()
                .id(generatedRandomPetId)
                .category(Category.builder()
                        .id(generatedRandomCategoryId)
                        .name(generatedRandomCategoryName)
                        .build())
                .name(generatedRandomPetName)
                .photoUrls(randomPhotoUrls)
                .tags(List.of(Tag.builder()
                        .id(generatedRandomTagId)
                        .name(generatedRandomTagName)
                        .build()))
                .status("available")
                .build());

        assertThat(addNewPetResponse.getStatusCode().is2xxSuccessful()).isTrue();

        assertWithRetry(
                () -> assertThat(petClient.findPetById(String.valueOf(generatedRandomPetId)).getStatusCode().is2xxSuccessful()).isTrue(),
                findByStatusEndpointDelay,
                java.time.Duration.ofMillis(findByStatusEndpointDelay),
                AssertionError.class
        );

        Pet foundPetById = objectMapper.convertValue(petClient.findPetById(String.valueOf(generatedRandomPetId)).getBody(), Pet.class);

        assertAll(
                () -> assertThat(foundPetById.getCategory().getId()).isEqualTo(generatedRandomCategoryId),
                () -> assertThat(foundPetById.getCategory().getName()).isEqualTo(generatedRandomCategoryName),
                () -> assertThat(foundPetById.getName()).isEqualTo(generatedRandomPetName),
                () -> assertThat(foundPetById.getPhotoUrls()).isEqualTo(randomPhotoUrls),
                () -> assertThat(foundPetById.getTags().getFirst().getId()).isEqualTo(generatedRandomTagId),
                () -> assertThat(foundPetById.getTags().getFirst().getName()).isEqualTo(generatedRandomTagName)
        );
    }

    @Test
    public void notCreatedPet_findByIdReturnsError() {
        Long generatedRandomPetId = petService.generateNotExistedPetId();

        assertThat(petClient.findPetById(String.valueOf(generatedRandomPetId)).getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404))).isTrue();
    }

    @ParameterizedTest(name = "[{index}] status={0}")
    @ValueSource(strings = {"!?&", "-1", "abc123"})
    public void invalidPetIdSupplied_findByIdReturnsError(String petId) {
        assertThat(petClient.findPetById(petId).getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404))).isTrue();
    }
}
