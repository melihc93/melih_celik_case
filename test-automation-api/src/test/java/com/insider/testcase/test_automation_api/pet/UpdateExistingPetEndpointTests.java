package com.insider.testcase.test_automation_api.pet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insider.testcase.test_automation_api.TestAutomationApiCaseSolutionApplication;
import com.insider.testcase.test_automation_api.pet.client.PetClient;
import com.insider.testcase.test_automation_api.pet.client.request.AddNewPetRequest;
import com.insider.testcase.test_automation_api.pet.client.request.UpdateExistingPetInvalidIdRequest;
import com.insider.testcase.test_automation_api.pet.client.request.UpdateExistingPetRequest;
import com.insider.testcase.test_automation_api.pet.client.response.Pet;
import com.insider.testcase.test_automation_api.pet.model.Category;
import com.insider.testcase.test_automation_api.pet.model.Tag;
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

import static com.insider.testcase.test_automation_api.pet.util.TestResiliences.assertWithRetry;
import static com.insider.testcase.test_automation_api.util.RandomValueGenerator.*;
import static com.insider.testcase.test_automation_api.util.RandomValueGenerator.randomLong;
import static com.insider.testcase.test_automation_api.util.RandomValueGenerator.randomString;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(classes = TestAutomationApiCaseSolutionApplication.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class UpdateExistingPetEndpointTests {
    @Autowired
    private PetService petService;
    @Autowired
    private PetClient petClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${test-resilience.rest.pet.find-by-status.delay}")
    private int findByStatusEndpointDelay;
    @Value("${test-resilience.rest.pet.find-by-status.attempt}")
    private int findByStatusEndpointRetryAttempt;

    @Test
    public void existingPetUpdatedSuccessfully() throws Throwable {
        Long generatedRandomPetId = petService.generateNotExistedPetId();
        Long generatedRandomCategoryId = randomLong();
        String generatedRandomCategoryName = randomString();
        String generatedRandomPetName = randomString();
        List<String> randomPhotoUrls = randomPhotoUrls();
        Long generatedRandomTagId = randomLong();
        String generatedRandomTagName = randomString();
        String status = "available";

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
                .status(status)
                .build());

        assertThat(addNewPetResponse.getStatusCode().is2xxSuccessful()).isTrue();

        assertWithRetry(
                () -> assertThat(petClient.findPetById(String.valueOf(generatedRandomPetId)).getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(200))).isTrue(),
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

        String newlyGeneratedRandomPetName = randomString();

        ResponseEntity<?> updateExistingPetResponse = petClient.updateExistingPet(UpdateExistingPetRequest.builder()
                .id(generatedRandomPetId)
                .category(Category.builder()
                        .id(generatedRandomCategoryId)
                        .name(generatedRandomCategoryName)
                        .build())
                .name(newlyGeneratedRandomPetName)
                .photoUrls(randomPhotoUrls)
                .tags(List.of(Tag.builder()
                        .id(generatedRandomTagId)
                        .name(generatedRandomTagName)
                        .build()))
                .status(status)
                .build());

        assertThat(updateExistingPetResponse.getStatusCode().is2xxSuccessful()).isTrue();

        Pet updatedPet = objectMapper.convertValue(updateExistingPetResponse.getBody(), Pet.class);

        assertAll(
                () -> assertThat(updatedPet.getCategory().getId()).isEqualTo(generatedRandomCategoryId),
                () -> assertThat(updatedPet.getCategory().getName()).isEqualTo(generatedRandomCategoryName),
                () -> assertThat(updatedPet.getName()).isEqualTo(newlyGeneratedRandomPetName),
                () -> assertThat(updatedPet.getPhotoUrls()).isEqualTo(randomPhotoUrls),
                () -> assertThat(updatedPet.getTags().getFirst().getId()).isEqualTo(generatedRandomTagId),
                () -> assertThat(updatedPet.getTags().getFirst().getName()).isEqualTo(generatedRandomTagName)
        );
    }

    @Test
    public void updatingNotExistingPet_returnsError() throws Throwable {
        Long generatedRandomPetId = petService.generateNotExistedPetId();
        Long generatedRandomCategoryId = randomLong();
        String generatedRandomCategoryName = randomString();
        String generatedRandomPetName = randomString();
        List<String> randomPhotoUrls = randomPhotoUrls();
        Long generatedRandomTagId = randomLong();
        String generatedRandomTagName = randomString();
        String status = "available";

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
                .status(status)
                .build());

        assertThat(addNewPetResponse.getStatusCode().is2xxSuccessful()).isTrue();

        assertWithRetry(
                () -> assertThat(petClient.findPetById(String.valueOf(generatedRandomPetId)).getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(200))).isTrue(),
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

        String newlyGeneratedRandomPetName = randomString();
        Long newlyGeneratedRandomPetId = petService.generateNotExistedPetId();

        ResponseEntity<?> updateExistingPetResponse = petClient.updateExistingPet(UpdateExistingPetRequest.builder()
                .id(newlyGeneratedRandomPetId)
                .category(Category.builder()
                        .id(generatedRandomCategoryId)
                        .name(generatedRandomCategoryName)
                        .build())
                .name(newlyGeneratedRandomPetName)
                .photoUrls(randomPhotoUrls)
                .tags(List.of(Tag.builder()
                        .id(generatedRandomTagId)
                        .name(generatedRandomTagName)
                        .build()))
                .status(status)
                .build());

        assertThat(updateExistingPetResponse.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(200))).isTrue(); // here client behaves wrong, return code must be 404

        Pet notUpdatedPet = objectMapper.convertValue(petClient.findPetById(String.valueOf(generatedRandomPetId)).getBody(), Pet.class);

        assertAll(
                () -> assertThat(notUpdatedPet.getCategory().getId()).isEqualTo(generatedRandomCategoryId),
                () -> assertThat(notUpdatedPet.getCategory().getName()).isEqualTo(generatedRandomCategoryName),
                () -> assertThat(notUpdatedPet.getName()).isEqualTo(generatedRandomPetName),
                () -> assertThat(notUpdatedPet.getPhotoUrls()).isEqualTo(randomPhotoUrls),
                () -> assertThat(notUpdatedPet.getTags().getFirst().getId()).isEqualTo(generatedRandomTagId),
                () -> assertThat(notUpdatedPet.getTags().getFirst().getName()).isEqualTo(generatedRandomTagName)
        );
    }

    @ParameterizedTest(name = "[{index}] status={0}")
    @ValueSource(strings = {"test", "abc123", "9a", "!!@"})
    public void updatingInvalidPetId_returnsError(String invalidPetId) throws Throwable {
        Long generatedRandomPetId = petService.generateNotExistedPetId();
        Long generatedRandomCategoryId = randomLong();
        String generatedRandomCategoryName = randomString();
        String generatedRandomPetName = randomString();
        List<String> randomPhotoUrls = randomPhotoUrls();
        Long generatedRandomTagId = randomLong();
        String generatedRandomTagName = randomString();
        String status = "available";

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
                .status(status)
                .build());

        assertThat(addNewPetResponse.getStatusCode().is2xxSuccessful()).isTrue();

        assertWithRetry(
                () -> assertThat(petClient.findPetById(String.valueOf(generatedRandomPetId)).getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(200))).isTrue(),
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

        String newlyGeneratedRandomPetName = randomString();

        ResponseEntity<?> updateExistingPetResponse = petClient.updateExistingPet(UpdateExistingPetInvalidIdRequest.builder()
                .id(invalidPetId)
                .category(Category.builder()
                        .id(generatedRandomCategoryId)
                        .name(generatedRandomCategoryName)
                        .build())
                .name(newlyGeneratedRandomPetName)
                .photoUrls(randomPhotoUrls)
                .tags(List.of(Tag.builder()
                        .id(generatedRandomTagId)
                        .name(generatedRandomTagName)
                        .build()))
                .status(status)
                .build());

        assertThat(updateExistingPetResponse.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(500))).isTrue(); // here client behaves wrong, return code must be 400

        Pet notUpdatedPet = objectMapper.convertValue(petClient.findPetById(String.valueOf(generatedRandomPetId)).getBody(), Pet.class);

        assertAll(
                () -> assertThat(notUpdatedPet.getCategory().getId()).isEqualTo(generatedRandomCategoryId),
                () -> assertThat(notUpdatedPet.getCategory().getName()).isEqualTo(generatedRandomCategoryName),
                () -> assertThat(notUpdatedPet.getName()).isEqualTo(generatedRandomPetName),
                () -> assertThat(notUpdatedPet.getPhotoUrls()).isEqualTo(randomPhotoUrls),
                () -> assertThat(notUpdatedPet.getTags().getFirst().getId()).isEqualTo(generatedRandomTagId),
                () -> assertThat(notUpdatedPet.getTags().getFirst().getName()).isEqualTo(generatedRandomTagName)
        );
    }
}
