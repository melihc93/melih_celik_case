package com.insider.testcase.test_automation.util;

public class UrlUtils {

    public static String encode(String urlSample) {
        return java.net.URLEncoder.encode(urlSample, java.nio.charset.StandardCharsets.UTF_8);
    }
}
