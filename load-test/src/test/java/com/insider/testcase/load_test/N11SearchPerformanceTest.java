package com.insider.testcase.load_test;

import static com.insider.testcase.load_test.util.DateUtil.getCurrentTime;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class N11SearchPerformanceTest extends LoadTestBase{

    @Test
    void search_flow_smokeLoadTest() throws Exception {
        boolean waf = WAF();

        var homeAssertion = waf
                ? responseAssertion().containsSubstrings("You are unable to access", "Cloudflare")
                : responseAssertion().containsSubstrings("n11");

        var searchIphoneAssertion = waf
                ? responseAssertion().containsSubstrings("You are unable to access")
                : responseAssertion().containsSubstrings("iphone");

        var searchCayAssertion = waf
                ? responseAssertion().containsSubstrings("You are unable to access")
                : responseAssertion().containsSubstrings("çay");

        var merchantAssertion = waf
                ? responseAssertion().containsSubstrings("You are unable to access")
                : responseAssertion().containsSubstrings("teknosa");

        TestPlanStats stats = testPlan(
                defaults(),
                headers(),

                threadGroup(SMOKE_TEST_THREADS(), SMOKE_TEST_ITERATIONS(),
                        httpSampler("n11 home page opens", "/").children(homeAssertion),

                        httpSampler("searching 'iphone'", "/arama")
                                .param("q", "iphone")
                                .children(searchIphoneAssertion),

                        httpSampler("searching 'çaydanlık'", "/arama")
                                .param("q", "çaydanlık")
                                .children(searchCayAssertion),

                        httpSampler("searching 'iphone' at page two", "/arama")
                                .param("q", "iphone")
                                .param("pg", "2")
                                .children(searchIphoneAssertion),

                        httpSampler("searching 'teknosa'", "/arama")
                                .param("q", "teknosa")
                                .children(merchantAssertion)
                ),

                htmlReporter("target/jmeter-report/smoke-load-test-" + getCurrentTime())
        ).run();

        assertNoErrorsIfNormal(stats);
    }

    @Test
    void search_iphone_spikeLoadTest() throws Exception {
        boolean waf = WAF();
        var iphoneAssertion = waf
                ? responseAssertion().containsSubstrings("You are unable to access", "Cloudflare")
                : responseAssertion().containsSubstrings("iphone");

        TestPlanStats stats = testPlan(
                defaults(),
                headers(),
                threadGroup()
                        .rampToAndHold(SPIKE_BASE_USERS(), SPIKE_PRE_RAMP_DURATION(), SPIKE_PRE_HOLD_DURATION())
                        .rampToAndHold(SPIKE_USERS(), SPIKE_RAMP_UP_DURATION(), SPIKE_HOLD_PEAK_DURATION())
                        .rampTo(FINAL_USERS(), SPIKE_RAMP_DOWN_DURATION())
                        .children(
                                httpSampler("search 'iphone' spike", "/arama")
                                        .param("q", "iphone")
                                        .children(iphoneAssertion)
                        ),
                htmlReporter("target/jmeter-report/spike-load-test-" + getCurrentTime())
        ).run();

        assertNoErrorsIfNormal(stats);
    }
}
