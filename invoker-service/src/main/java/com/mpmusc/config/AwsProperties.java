package com.mpmusc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {

    private String accessKey;
    private String secretKey;
    private Lambda lambda;
    private String region;
    private BigDecimal estimatedCost;
    private Integer concurrency;

    @Data
    public static class Lambda {
        private String functionName;
    }

}