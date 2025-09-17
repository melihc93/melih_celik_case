package com.insider.testcase.load_test.config;

import com.insider.testcase.load_test.config.model.LoadTestConfig;
import com.insider.testcase.load_test.util.EnvironmentExpander;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public final class ConfigManager {
    private static volatile LoadTestConfig INSTANCE;

    private ConfigManager() {}

    public static LoadTestConfig get() {
        if (INSTANCE == null) {
            synchronized (ConfigManager.class) {
                if (INSTANCE == null) INSTANCE = load();
            }
        }
        return INSTANCE;
    }

    private static LoadTestConfig load() {
        String name = System.getProperty("CONFIG", "loadtest.yaml");
        String expanded = null;

        // 1) classpath
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name)) {
            if (is != null) {
                expanded = EnvironmentExpander.expand(new String(is.readAllBytes(), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // 2) filesystem fallback
        if (expanded == null) {
            Path p = Paths.get(name);
            if (Files.exists(p)) {
                try {
                    expanded = EnvironmentExpander.expand(Files.readString(p));
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to read config: " + p, e);
                }
            }
        }

        if (expanded == null) {
            // No file found: build a default config
            LoadTestConfig def = new LoadTestConfig();
            return def;
        }

        // Important: parse the EXPANDED text
        return new Yaml().loadAs(expanded, LoadTestConfig.class);
    }
}