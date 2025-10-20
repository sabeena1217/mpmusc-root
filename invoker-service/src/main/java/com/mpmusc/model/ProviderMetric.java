package com.mpmusc.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "provider_metrics_2")
public class ProviderMetric {
    @Id @GeneratedValue
    private Long id;

    private String provider;
    // Total time including network latency
    private Long totalTimeMs;
    // Actual execution time of the function
    private Long executionTimeMs;
    private boolean isError;
    private String errorMsg;
    private String region;
    private BigDecimal cost;
    // remaining concurrency
    private Integer concurrency;
    private LocalDateTime recordedAt;

    // constructors, getters/setters
    @PrePersist
    public void prePersist() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }

}
