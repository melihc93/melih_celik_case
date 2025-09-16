package com.insider.testcase.test_automation_api.pet.client.response;

import com.insider.testcase.test_automation_api.pet.client.model.Category;
import com.insider.testcase.test_automation_api.pet.client.model.Tag;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Pet {
    private Long id;
    private Category category;
    private String name;
    private List<String> photoUrls;
    private List<Tag> tags;
    private String status;
}
