package com.insider.testcase.test_automation_api.pet.client.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddNewPetInvalidBody {
    private String id;
    private Long name;
}
