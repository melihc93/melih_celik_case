package com.insider.testcase.test_automation_api.pet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insider.testcase.test_automation_api.TestAutomationApiCaseSolutionApplication;
import com.insider.testcase.test_automation_api.pet.client.PetClient;
import com.insider.testcase.test_automation_api.pet.client.request.AddNewPetRequest;
import com.insider.testcase.test_automation_api.pet.client.response.ErrorResponse;
import com.insider.testcase.test_automation_api.pet.client.response.Pet;
import com.insider.testcase.test_automation_api.pet.client.response.UploadImageResponse;
import com.insider.testcase.test_automation_api.pet.model.Category;
import com.insider.testcase.test_automation_api.pet.model.Tag;
import com.insider.testcase.test_automation_api.pet.service.PetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestConstructor;

import java.util.List;

import static com.insider.testcase.test_automation_api.pet.util.TestResiliences.assertWithRetry;
import static com.insider.testcase.test_automation_api.util.RandomValueGenerator.randomLong;
import static com.insider.testcase.test_automation_api.util.RandomValueGenerator.randomString;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(classes = TestAutomationApiCaseSolutionApplication.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class UploadImageEndpointTests {
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
    public void successfullyUpdateImageOfPet() throws Throwable {
        Long generatedRandomPetId = petService.generateNotExistedPetId();
        Long generatedRandomCategoryId = randomLong();
        String generatedRandomCategoryName = randomString();
        String generatedRandomPetName = randomString();
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
                () -> assertThat(foundPetById.getPhotoUrls()).isNull(),
                () -> assertThat(foundPetById.getTags().getFirst().getId()).isEqualTo(generatedRandomTagId),
                () -> assertThat(foundPetById.getTags().getFirst().getName()).isEqualTo(generatedRandomTagName)
        );


        ClassPathResource cpr = new ClassPathResource("cute-cat.png");
        String additionalMetadata = "test123";

        ResponseEntity<?> uploadRes = petClient.uploadImage(
                String.valueOf(generatedRandomPetId),
                additionalMetadata,
                cpr,
                MediaType.IMAGE_PNG
        );

        assertThat(uploadRes.getStatusCode().is2xxSuccessful()).isTrue();

        UploadImageResponse body = objectMapper.convertValue(uploadRes.getBody(), UploadImageResponse.class);
        assertThat(body).isNotNull();
        assertAll(
                () -> assertThat(body.getMessage()).containsIgnoringCase("test123"),
                () -> assertThat(body.getMessage()).contains("cute-cat.png")
        );
    }

    @Test
    public void notExistedPetId_imageNotUpdatedByItsEndpoint() throws Throwable {
        Long generatedRandomPetId = petService.generateNotExistedPetId();
        Long generatedRandomCategoryId = randomLong();
        String generatedRandomCategoryName = randomString();
        String generatedRandomPetName = randomString();
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
                () -> assertThat(foundPetById.getPhotoUrls()).isNull(),
                () -> assertThat(foundPetById.getTags().getFirst().getId()).isEqualTo(generatedRandomTagId),
                () -> assertThat(foundPetById.getTags().getFirst().getName()).isEqualTo(generatedRandomTagName)
        );


        ClassPathResource cpr = new ClassPathResource("cute-cat.png");
        String additionalMetadata = "test123";

        ResponseEntity<?> uploadRes = petClient.uploadImage(
                null,
                additionalMetadata,
                cpr,
                MediaType.IMAGE_PNG
        );

        assertThat(uploadRes.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404))).isTrue();

        ErrorResponse body = objectMapper.convertValue(uploadRes.getBody(), ErrorResponse.class);
        assertAll(
                () -> assertThat(body.getCode()).isEqualTo(404),
                () -> assertThat(body.getMessage()).isEqualTo("java.lang.NumberFormatException: For input string: \"null\""),
                () -> assertThat(body.getType()).isEqualTo("unknown")
        );
    }
}
