package com.insider.testcase.load_test.n11;

public enum DefaultHeader {
    USER_AGENT("User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"),
    ACCEPT("Accept",
            "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
    ACCEPT_LANGUAGE("Accept-Language",
            "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7"),
    ACCEPT_ENCODING("Accept-Encoding",
            "gzip, deflate");

    private final String key;
    private final String value;

    DefaultHeader(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String key()   { return key; }
    public String value() { return value; }
}

