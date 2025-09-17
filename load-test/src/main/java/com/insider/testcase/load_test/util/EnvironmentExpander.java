package com.insider.testcase.load_test.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EnvironmentExpander {
    private EnvironmentExpander() {}

    private static final Pattern P =
            Pattern.compile("\\$\\{([A-Za-z0-9_.\\-]+)(?::([^}]*))?}", Pattern.UNICODE_CASE);

    public static String expand(String input) {
        if (input == null || input.isBlank()) return input;
        Matcher m = P.matcher(input);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            String def = m.group(2);
            String val = System.getProperty(key);
            if (val == null) val = System.getenv(key);
            if (val == null) val = def;
            if (val == null) val = "";
            m.appendReplacement(out, Matcher.quoteReplacement(val));
        }
        m.appendTail(out);
        return out.toString();
    }
}

