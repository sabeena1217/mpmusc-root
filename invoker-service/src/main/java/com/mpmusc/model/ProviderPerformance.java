package com.mpmusc.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "provider_performance", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "concurrency"})
})
public class ProviderPerformance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String provider;

    private Integer concurrency;

    @Column(name = "median_time_ms")
    private Double medianTimeMs;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt = LocalDateTime.now();

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public Integer getConcurrency() { return concurrency; }
    public void setConcurrency(Integer concurrency) { this.concurrency = concurrency; }

    public Double getMedianTimeMs() { return medianTimeMs; }
    public void setMedianTimeMs(Double medianTimeMs) { this.medianTimeMs = medianTimeMs; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}

/*

CREATE TABLE provider_performance (
  id SERIAL PRIMARY KEY,
  provider VARCHAR(50) NOT NULL,
  concurrency INTEGER NOT NULL,
  median_time_ms DOUBLE PRECISION NOT NULL,
  recorded_at TIMESTAMP DEFAULT now(),
  UNIQUE (provider, concurrency)
);

CREATE INDEX idx_provider_concurrency ON provider_performance(provider, concurrency);

INSERT INTO provider_performance (provider, concurrency, median_time_ms) VALUES
('aws', 1, 1087.5), ('azure', 1, 936), ('openwhisk', 1, 601.5),
('aws', 2, 1239.5), ('azure', 2, 928.5), ('openwhisk', 2, 928),
('aws', 3, 1215.5), ('azure', 3, 958.5), ('openwhisk', 3, 1261.5),
('aws', 4, 1101),   ('azure', 4, 995),   ('openwhisk', 4, 1770.5),
('aws', 5, 1157.5), ('azure', 5, 1111.5), ('openwhisk', 5, 2086),
('aws', 6, 1306),   ('azure', 6, 1217.5), ('openwhisk', 6, 2445),
('aws', 7, 1568.5), ('azure', 7, 1308),   ('openwhisk', 7, 3062),
('aws', 8, 1680.5), ('azure', 8, 1381),   ('openwhisk', 8, 3189.5);

*/