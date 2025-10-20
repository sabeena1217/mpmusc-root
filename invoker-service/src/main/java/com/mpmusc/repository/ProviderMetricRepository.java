package com.mpmusc.repository;

import com.mpmusc.model.ProviderMetric;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProviderMetricRepository extends JpaRepository<ProviderMetric, Long> {
    @Query("SELECT pm FROM ProviderMetric pm WHERE pm.provider = :provider ORDER BY pm.recordedAt DESC")
    List<ProviderMetric> findRecentByProvider(@Param("provider") String provider, Pageable pageable);

    @Query("SELECT pm FROM ProviderMetric pm WHERE pm.recordedAt >= :cutoff")
    List<ProviderMetric> findRecentOverall(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT pm FROM ProviderMetric pm WHERE pm.recordedAt >= :since")
    List<ProviderMetric> findSince(@Param("since") LocalDateTime since);
}
