package com.insider.testcase.test_automation_api.pet.client.request;

public enum QueryParamNames {
    STATUS("status");

    private final String value;

    QueryParamNames(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
