package com.mpmusc.core;

import java.util.Map;

/**
 * Encapsulates the result of gender distribution analysis.
 */
public class GenderDistributionResult {
    private Map<String, Map<String, Double>> distribution;
    private long executionTimeMillis;
    private String error;

    public GenderDistributionResult() {
    }

    public GenderDistributionResult(Map<String, Map<String, Double>> distribution, long executionTimeMillis) {
        this.distribution = distribution;
        this.executionTimeMillis = executionTimeMillis;
    }

    public GenderDistributionResult(String error) {
        this.error = error;
    }

    public Map<String, Map<String, Double>> getDistribution() {
        return distribution;
    }

    public void setDistribution(Map<String, Map<String, Double>> distribution) {
        this.distribution = distribution;
    }

    public long getExecutionTimeMillis() {
        return executionTimeMillis;
    }

    public void setExecutionTimeMillis(long executionTimeMillis) {
        this.executionTimeMillis = executionTimeMillis;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
