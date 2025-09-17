package com.insider.testcase.test_automation.support;

import org.junit.jupiter.api.extension.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.*;
import java.nio.file.*;
import java.util.function.Supplier;

public class ScreenShotOnFailureExtension implements TestWatcher, BeforeTestExecutionCallback {

    private final Supplier<WebDriver> driverSupplier;
    private final Supplier<String> dirSupplier;
    private String currentTestName;

    public ScreenShotOnFailureExtension(Supplier<WebDriver> driverSupplier, Supplier<String> dirSupplier) {
        this.driverSupplier = driverSupplier;
        this.dirSupplier = dirSupplier;
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        currentTestName = context.getDisplayName();
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        try {
            WebDriver drv = driverSupplier.get();
            if (!(drv instanceof TakesScreenshot ts)) return;

            String dir = dirSupplier.get();

            Files.createDirectories(Paths.get(dir));
            String name = sanitize(currentTestName == null ? "test" : currentTestName);
            String tsStr = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path out = Paths.get(dir, name + "_" + tsStr + ".png");
            Files.write(out, ts.getScreenshotAs(OutputType.BYTES));
            System.out.println("Saved screenshot: " + out.toAbsolutePath());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
