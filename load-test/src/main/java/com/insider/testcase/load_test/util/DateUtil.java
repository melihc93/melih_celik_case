package com.insider.testcase.load_test.util;

public class DateUtil {
    public static String getCurrentTime() {
        return java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    }
}
