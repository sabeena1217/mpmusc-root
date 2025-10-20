package com.mpmusc.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "provider_avg_rtts")
@Data
public class ProviderAvgRtt {
    @Id
    @GeneratedValue
    private Long id;

    private String provider;
    private Integer concurrency;
    private Long averageRtt;
    private Integer invocationCount;   // how many calls used to compute
    private LocalDateTime recordedAt;

    @PrePersist
    public void prePersist() {
        if (recordedAt == null) recordedAt = LocalDateTime.now();
    }
}

