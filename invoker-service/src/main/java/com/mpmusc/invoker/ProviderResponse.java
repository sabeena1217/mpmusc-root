package com.mpmusc.invoker;

import lombok.Data;

@Data
public class ProviderResponse {

    private final String payload;
    private final boolean success;
    private final String errorMsg;
    private long executionTimeMs;
//    private final BigDecimal cost;
//    private final String region;

    public ProviderResponse(String payload, boolean success, String errorMsg, long executionTimeMs/*, BigDecimal cost, String region*/) {
        this.payload = payload;
        this.success = success;
        this.errorMsg = errorMsg;
        this.executionTimeMs = executionTimeMs;
//        this.cost = cost;
//        this.region = region;
    }
//    public String getPayload() { return payload; }
//    public boolean isSuccess() { return success; }
//    public BigDecimal getCost() { return cost; }

}
