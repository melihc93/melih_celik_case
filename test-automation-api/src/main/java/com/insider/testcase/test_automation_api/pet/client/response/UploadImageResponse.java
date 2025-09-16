package com.insider.testcase.test_automation_api.pet.client.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UploadImageResponse {
    private int code;
    private String type;
    private String message;
}
