package com.mpmusc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "azure")
public class AzureProperties {

    private Function functionUrl;
    private String region;
    private BigDecimal estimatedCost;
    private Integer concurrency;

    @Data
    public static class Function {
        private String url;
    }

}
