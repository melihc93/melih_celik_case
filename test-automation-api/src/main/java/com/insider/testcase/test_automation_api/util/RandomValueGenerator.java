package com.insider.testcase.test_automation_api.util;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class RandomValueGenerator {
    private static final String[] DOMAINS = {
            "https://img.example.com",
            "https://cdn.example.org",
            "https://media.example.net"
    };
    private static final String[] EXT = {"jpg", "png", "webp"};
    private static final int LENGTH_OF_URI = 10;
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int RANDOM_STRING_LENGTH = 10;

    public static Long randomLong() {
        return ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE);
    }

    public static String randomString() {
        var r = ThreadLocalRandom.current();
        var sb = new StringBuilder(RANDOM_STRING_LENGTH);
        for (int i = 0; i < RANDOM_STRING_LENGTH; i++) {
            sb.append(ALPHANUM.charAt(r.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }

    public static List<String> randomPhotoUrls() {
        var r = ThreadLocalRandom.current();
        return IntStream.range(0, Math.max(0, LENGTH_OF_URI))
                .mapToObj(i -> {
                    String domain = DOMAINS[r.nextInt(DOMAINS.length)];
                    String name = UUID.randomUUID().toString().replace("-", "");
                    String ext = EXT[r.nextInt(EXT.length)];
                    return String.format("%s/pets/%s.%s", domain, name, ext);
                })
                .toList();
    }
}
