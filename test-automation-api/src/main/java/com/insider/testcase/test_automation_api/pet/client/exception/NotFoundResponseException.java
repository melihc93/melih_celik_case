package com.insider.testcase.test_automation_api.pet.client.exception;

import org.springframework.http.ResponseEntity;

public class NotFoundResponseException extends RuntimeException {
    private final ResponseEntity<?> lastResponse;

    public NotFoundResponseException(String message, ResponseEntity<?> lastResponse) {
        super(message);
        this.lastResponse = lastResponse;
    }

    public ResponseEntity<?> getLastResponse() {
        return lastResponse;
    }
}
