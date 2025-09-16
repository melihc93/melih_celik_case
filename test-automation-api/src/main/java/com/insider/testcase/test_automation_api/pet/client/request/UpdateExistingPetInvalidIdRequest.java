package com.insider.testcase.test_automation_api.pet.client.request;

import com.insider.testcase.test_automation_api.pet.model.Category;
import com.insider.testcase.test_automation_api.pet.model.Tag;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExistingPetInvalidIdRequest {
    private String id;
    private Category category;
    private String name;
    private List<String> photoUrls;
    private List<Tag> tags;
    private String status;
}
