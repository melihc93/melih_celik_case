package com.insider.testcase.load_test.config;

import org.apache.jmeter.util.JMeterUtils;

import java.io.InputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public final class JMeterPropsLoader {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private JMeterPropsLoader() {}

    public static void init() {
        if (!INITIALIZED.compareAndSet(false, true)) return;

        String name = System.getProperty("JMETER_PROPS", "jmeter.properties");
        Properties p = new Properties();

        try (InputStream is = JMeterPropsLoader.class.getResourceAsStream("/" + name)) {
            if (is != null) {
                p.load(is);
            } else {
                Path path = Paths.get(name);
                if (Files.exists(path)) {
                    try (InputStream fis = Files.newInputStream(path)) {
                        p.load(fis);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load JMeter properties '" + name + "'", e);
        }

        p.forEach((k, v) -> JMeterUtils.setProperty(k.toString(), v.toString()));
    }
}
