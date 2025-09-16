package com.insider.testcase.test_automation_api.pet.util;

import org.junit.jupiter.api.function.Executable;

import java.time.Duration;

public class TestResiliences {

    public static void assertWithRetry(Executable assertion) throws Throwable {
        int attempts = Math.max(1, Integer.getInteger("test.retry.attempts", 3));
        long delayMs = Math.max(0, Long.getLong("test.retry.delayMs", 500L));
        assertWithRetry(assertion, attempts, Duration.ofMillis(delayMs), AssertionError.class);
    }

    @SafeVarargs
    public static void assertWithRetry(Executable assertion,
                                       int attempts,
                                       Duration delay,
                                       Class<? extends Throwable>... retryOn) throws Throwable {
        attempts = Math.max(1, attempts);
        Throwable last = null;

        for (int i = 1; i <= attempts; i++) {
            try {
                assertion.execute();
                return; // success
            } catch (Throwable t) {
                last = t;
                if (!isRetryable(t, retryOn) || i == attempts) throw t;
                sleep(delay);
            }
        }
        if (last != null) throw last;
    }

    private static boolean isRetryable(Throwable t, Class<? extends Throwable>[] types) {
        if (types == null || types.length == 0) return t instanceof AssertionError;
        for (Class<? extends Throwable> c : types) if (c.isInstance(t)) return true;
        return false;
    }

    private static void sleep(Duration d) {
        try { Thread.sleep(Math.max(0, d.toMillis())); }
        catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw new RuntimeException(ie); }
    }
}
