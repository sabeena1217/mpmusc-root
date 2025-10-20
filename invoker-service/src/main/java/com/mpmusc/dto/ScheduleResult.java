package com.mpmusc.dto;

import java.util.Map;


public class ScheduleResult {
    // allocation planned by the optimizer
    private Map<String, Integer> allocation;
    // estimated times from median table (for allocated counts)
    private Map<String, Double> providerEstimatedTimesMs;
    private Double estimatedCompletionTimeMs;

    // actual observed results after executing the allocation
    private Map<String, Double> providerActualAvgRttMs;    // average of successful rtts per provider (0 if none)
    private Map<String, Integer> providerSuccessCount;     // number of successful invocations per provider
    private Map<String, Integer> providerFailureCount;     // number of failed invocations per provider
    private Double actualCompletionTimeMs;                 // measured wall-clock makespan (ms)

    // --- getters / setters ---
    public Map<String, Integer> getAllocation() { return allocation; }
    public void setAllocation(Map<String, Integer> allocation) { this.allocation = allocation; }

    public Map<String, Double> getProviderEstimatedTimesMs() { return providerEstimatedTimesMs; }
    public void setProviderEstimatedTimesMs(Map<String, Double> providerEstimatedTimesMs) { this.providerEstimatedTimesMs = providerEstimatedTimesMs; }

    public Double getEstimatedCompletionTimeMs() { return estimatedCompletionTimeMs; }
    public void setEstimatedCompletionTimeMs(Double estimatedCompletionTimeMs) { this.estimatedCompletionTimeMs = estimatedCompletionTimeMs; }

    public Map<String, Double> getProviderActualAvgRttMs() { return providerActualAvgRttMs; }
    public void setProviderActualAvgRttMs(Map<String, Double> providerActualAvgRttMs) { this.providerActualAvgRttMs = providerActualAvgRttMs; }

    public Map<String, Integer> getProviderSuccessCount() { return providerSuccessCount; }
    public void setProviderSuccessCount(Map<String, Integer> providerSuccessCount) { this.providerSuccessCount = providerSuccessCount; }

    public Map<String, Integer> getProviderFailureCount() { return providerFailureCount; }
    public void setProviderFailureCount(Map<String, Integer> providerFailureCount) { this.providerFailureCount = providerFailureCount; }

    public Double getActualCompletionTimeMs() { return actualCompletionTimeMs; }
    public void setActualCompletionTimeMs(Double actualCompletionTimeMs) { this.actualCompletionTimeMs = actualCompletionTimeMs; }
}