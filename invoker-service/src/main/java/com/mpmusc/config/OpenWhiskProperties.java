package com.mpmusc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "openwhisk")
public class OpenWhiskProperties {

    private String apiHost;
    private String namespace;
    private String action;
    private String apiKey;
    private String region;
    private BigDecimal estimatedCost;
    private Integer concurrency;

}
