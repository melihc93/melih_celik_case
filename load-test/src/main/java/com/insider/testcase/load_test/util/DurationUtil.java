package com.insider.testcase.load_test.util;

import java.time.Duration;

public final class DurationUtil {
    private DurationUtil() {}
    public static Duration parse(String raw) {
        if (raw == null || raw.isBlank()) return Duration.ZERO;
        String s = raw.trim().toLowerCase();
        try {
            if (s.startsWith("pt")) return Duration.parse(s.toUpperCase()); // ISO-8601 "PT10S"
            if (s.endsWith("ms")) return Duration.ofMillis(Long.parseLong(s.substring(0, s.length()-2)));
            if (s.endsWith("s"))  return Duration.ofSeconds(Long.parseLong(s.substring(0, s.length()-1)));
            if (s.endsWith("m"))  return Duration.ofMinutes(Long.parseLong(s.substring(0, s.length()-1)));
            if (s.endsWith("h"))  return Duration.ofHours(Long.parseLong(s.substring(0, s.length()-1)));
            return Duration.parse("PT" + s.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid duration: " + raw, e);
        }
    }
}
