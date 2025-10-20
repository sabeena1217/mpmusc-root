package com.mpmusc.invoker;

import java.math.BigDecimal;
import java.util.Map;

public interface FunctionInvoker {
    /** Invoke the analysis function for the given input filename. */
    ProviderResponse invoke(String filename) throws Exception;

    /**
     * Invoke the analysis function.
     * @param input  Any key/value map you need to send as payload.
     * @return       A ProviderResponse with raw payload, success flag, and cost.
     */
//    ProviderResponse invoke(Map<String,Object> input) throws Exception;

    /**
     * Predefined concurrency for this provider
     */
    String getRegion();

    /**
     * Predefined estimatedCost for this provider
     */
    BigDecimal getEstimatedCost();

    /**
     * Predefined concurrency for this provider
     */
    int getConcurrency();
}
