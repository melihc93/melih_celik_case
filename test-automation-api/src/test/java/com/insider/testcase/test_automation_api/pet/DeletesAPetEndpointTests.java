package com.insider.testcase.test_automation_api.pet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insider.testcase.test_automation_api.TestAutomationApiCaseSolutionApplication;
import com.insider.testcase.test_automation_api.pet.client.PetClient;
import com.insider.testcase.test_automation_api.pet.client.request.AddNewPetRequest;
import com.insider.testcase.test_automation_api.pet.client.response.DeleteByIdResponse;
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

import static com.insider.testcase.test_automation_api.util.RandomValueGenerator.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(classes = TestAutomationApiCaseSolutionApplication.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class DeletesAPetEndpointTests {
    @Autowired
    private PetClient petClient;
    @Autowired
    private PetService petService;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${test-resilience.rest.pet.find-by-status.delay}")
    private int findByStatusEndpointDelay;
    @Value("${test-resilience.rest.pet.find-by-status.attempt}")
    private int findByStatusEndpointRetryAttempt;

    @Test
    public void successfullyDeletePetWithId() {
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

        ResponseEntity<?> deletePetByIdResponse = petClient.deletePetById(String.valueOf(generatedRandomPetId));

        assertThat(deletePetByIdResponse.getStatusCode().is2xxSuccessful()).isTrue();

        DeleteByIdResponse deleteByIdResponseBody = objectMapper.convertValue(deletePetByIdResponse.getBody(), DeleteByIdResponse.class);
        assertAll(
                () -> assertThat(deleteByIdResponseBody).isNotNull(),
                () -> assertThat(deleteByIdResponseBody.getMessage()).contains(String.valueOf(generatedRandomPetId)),
                () -> assertThat(deleteByIdResponseBody.getCode()).isEqualTo(200)
        );
    }

    @Test
    public void notExistedPet_deleteRequestReturnError() {
        Long generatedRandomPetId = petService.generateNotExistedPetId();

        ResponseEntity<?> deletePetByIdResponse = petClient.deletePetById(String.valueOf(generatedRandomPetId));

        assertThat(deletePetByIdResponse.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404))).isTrue();
    }

    @ParameterizedTest(name = "[{index}] status={0}")
    @ValueSource(strings = {"-123", "test1", "!))a"})
    public void invalidPetId_deleteRequestReturnError(String petId) {
        ResponseEntity<?> deletePetByIdResponse = petClient.deletePetById(petId);

        assertThat(deletePetByIdResponse.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404))).isTrue(); // client behave unexpected here, it should return 400
    }
}
