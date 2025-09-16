package com.insider.testcase.test_automation_api.pet.service;

import com.insider.testcase.test_automation_api.pet.client.PetClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.insider.testcase.test_automation_api.util.RandomValueGenerator.randomLong;

@Service
@RequiredArgsConstructor
public class PetService {
    private final PetClient petClient;

    public Boolean isPetExists(Long petId) {
        return petClient.findPetById(String.valueOf(petId)).getStatusCode().is2xxSuccessful();
    }

    public Long generateNotExistedPetId() {
        Long randomId = randomLong();
        if (!this.isPetExists(randomId)) return randomId;
        this.generateNotExistedPetId();
        throw new RuntimeException("Can not generate random pet id");
    }
}
