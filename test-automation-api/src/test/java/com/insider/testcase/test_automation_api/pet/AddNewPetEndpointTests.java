package com.insider.testcase.test_automation_api.pet;

import com.insider.testcase.test_automation_api.TestAutomationApiCaseSolutionApplication;
import com.insider.testcase.test_automation_api.pet.client.PetClient;
import com.insider.testcase.test_automation_api.pet.client.request.AddNewPetInvalidBody;
import com.insider.testcase.test_automation_api.pet.model.Category;
import com.insider.testcase.test_automation_api.pet.model.Tag;
import com.insider.testcase.test_automation_api.pet.client.request.AddNewPetRequest;
import com.insider.testcase.test_automation_api.pet.service.PetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestConstructor;

import java.util.List;

import static com.insider.testcase.test_automation_api.util.RandomValueGenerator.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = TestAutomationApiCaseSolutionApplication.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class AddNewPetEndpointTests {
    @Autowired
    private PetClient petClient;
    @Autowired
    private PetService petService;

    @ParameterizedTest(name = "[{index}] status={0}")
    @ValueSource(strings = {"available", "sold", "pending"})
    public void addNewPet_successfullyCreated(String status) {
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
                .status(status)
                .build());

        assertThat(addNewPetResponse.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    public void addNewPetWithInvalidInput_petNotAdded() {
        String randomString = randomString();
        Long randomLong = randomLong();

        ResponseEntity<?> addNewPetResponse = petClient.addNewPet(AddNewPetInvalidBody.builder()
                .id(randomString)
                .name(randomLong)
                .build());

        assertThat(addNewPetResponse.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(500))).isTrue();
    }
}
