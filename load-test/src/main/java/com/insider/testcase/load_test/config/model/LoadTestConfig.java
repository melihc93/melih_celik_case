package com.insider.testcase.load_test.config.model;

import com.insider.testcase.load_test.util.DurationUtil;

import java.time.Duration;

public class LoadTestConfig {
    private String base;
    private String mode;
    private Timeouts timeouts;
    private SmokeLoadTest smokeLoadTest;
    private SpikeLoadTest spikeLoadTest;

    public static class Timeouts {
        private String connection;
        private String response;

        public String getConnection() {
            return connection;
        }

        public void setConnection(String connection) {
            this.connection = connection;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        public Duration connectionDuration() {
            return DurationUtil.parse(connection);
        }

        public Duration responseDuration() {
            return DurationUtil.parse(response);
        }
    }

    public static class SmokeLoadTest {
        private int iterations;
        private int threads;

        public int getIterations() {
            return iterations;
        }

        public void setIterations(int iterations) {
            this.iterations = iterations;
        }

        public int getThreads() {
            return threads;
        }

        public void setThreads(int threads) {
            this.threads = threads;
        }
    }

    public static class SpikeLoadTest {
        private int baseUsers;
        private String preRampTime;
        private String preHoldTime;
        private int spikeUsers;
        private String rampUpTime;
        private String holdPeakTime;
        private int finalUsers;
        private String rampDownTime;

        public int getBaseUsers() {
            return baseUsers;
        }

        public void setBaseUsers(int baseUsers) {
            this.baseUsers = baseUsers;
        }

        public String getPreRampTime() {
            return preRampTime;
        }

        public void setPreRampTime(String preRampTime) {
            this.preRampTime = preRampTime;
        }

        public int getSpikeUsers() {
            return spikeUsers;
        }

        public void setSpikeUsers(int spikeUsers) {
            this.spikeUsers = spikeUsers;
        }

        public String getRampUpTime() {
            return rampUpTime;
        }

        public Duration preRampTimeDuration() {
            return DurationUtil.parse(preRampTime);
        }

        public void setRampUpTime(String rampUpTime) {
            this.rampUpTime = rampUpTime;
        }

        public String getHoldPeakTime() {
            return holdPeakTime;
        }

        public void setHoldPeakTime(String holdPeakTime) {
            this.holdPeakTime = holdPeakTime;
        }

        public String getRampDownTime() {
            return rampDownTime;
        }

        public void setRampDownTime(String rampDownTime) {
            this.rampDownTime = rampDownTime;
        }

        public String getPreHoldTime() {
            return preHoldTime;
        }

        public void setPreHoldTime(String preHoldTime) {
            this.preHoldTime = preHoldTime;
        }

        public int getFinalUsers() {
            return finalUsers;
        }

        public void setFinalUsers(int finalUsers) {
            this.finalUsers = finalUsers;
        }

        public Duration rampUpDuration() {
            return DurationUtil.parse(rampUpTime);
        }

        public Duration holdPeakDuration() {
            return DurationUtil.parse(holdPeakTime);
        }

        public Duration rampDownDuration() {
            return DurationUtil.parse(rampDownTime);
        }

        public Duration preHoldDuration() {
            return DurationUtil.parse(preHoldTime);
        }
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Timeouts getTimeouts() {
        return timeouts;
    }

    public void setTimeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
    }

    public SmokeLoadTest getSmokeLoadTest() {
        return smokeLoadTest;
    }

    public void setSmokeLoadTest(SmokeLoadTest smokeLoadTest) {
        this.smokeLoadTest = smokeLoadTest;
    }

    public SpikeLoadTest getSpikeLoadTest() {
        return spikeLoadTest;
    }

    public void setSpikeLoadTest(SpikeLoadTest spikeLoadTest) {
        this.spikeLoadTest = spikeLoadTest;
    }
}