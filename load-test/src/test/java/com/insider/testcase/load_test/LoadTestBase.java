package com.insider.testcase.load_test;

import com.insider.testcase.load_test.config.ConfigManager;
import com.insider.testcase.load_test.config.model.LoadTestConfig;
import com.insider.testcase.load_test.n11.DefaultHeader;
import org.apache.jmeter.util.JMeterUtils;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpDefaults;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpHeaders;

public abstract class LoadTestBase {

    protected LoadTestConfig cfg = ConfigManager.get();

    protected boolean WAF() {
        return "waf".equalsIgnoreCase(cfg.getMode());
    }

    protected int SMOKE_TEST_ITERATIONS() {
        return cfg.getSmokeLoadTest().getIterations();
    }

    protected int SMOKE_TEST_THREADS() {
        return cfg.getSmokeLoadTest().getThreads();
    }

    protected int SPIKE_BASE_USERS() {
        return cfg.getSpikeLoadTest().getBaseUsers();
    }

    protected Duration SPIKE_PRE_RAMP_DURATION() {
        return cfg.getSpikeLoadTest().preRampTimeDuration();
    }

    protected Duration SPIKE_PRE_HOLD_DURATION() {
        return cfg.getSpikeLoadTest().preHoldDuration();
    }

    protected int SPIKE_USERS() {
        return cfg.getSpikeLoadTest().getSpikeUsers();
    }

    protected Duration SPIKE_RAMP_UP_DURATION() {
        return cfg.getSpikeLoadTest().rampUpDuration();
    }

    protected Duration SPIKE_HOLD_PEAK_DURATION() {
        return cfg.getSpikeLoadTest().holdPeakDuration();
    }

    protected int FINAL_USERS() {
        return cfg.getSpikeLoadTest().getFinalUsers();
    }

    protected Duration SPIKE_RAMP_DOWN_DURATION() {
        return cfg.getSpikeLoadTest().rampDownDuration();
    }

    protected DslTestPlan.TestPlanChild defaults() {
        return httpDefaults()
                .url(cfg.getBase())
                .encoding(StandardCharsets.UTF_8)
                .connectionTimeout(cfg.getTimeouts().connectionDuration())
                .responseTimeout(cfg.getTimeouts().responseDuration());
    }

    protected DslTestPlan.TestPlanChild headers() {
        var hdrs = httpHeaders();
        for (DefaultHeader h : DefaultHeader.values()) {
            hdrs = hdrs.header(h.key(), h.value());
        }
        return hdrs;
    }

    protected void assertNoErrorsIfNormal(TestPlanStats stats) {
        if (!WAF()) {
            assertEquals(0, stats.overall().errorsCount(),
                    "Failing samples exist. See target/jtls/error/*.jtl and the unified HTML report.");
            assertTrue(stats.overall().sampleTimePercentile99().compareTo(Duration.ofSeconds(5)) <= 0,
                    "p99 > 5s: " + stats.overall().sampleTimePercentile99());
        } else {
            System.out.println("[INFO] WAF mode: Cloudflare block expected.");
        }
    }
}
