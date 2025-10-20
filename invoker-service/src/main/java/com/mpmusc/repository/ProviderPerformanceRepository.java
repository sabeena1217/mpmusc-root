package com.mpmusc.repository;

import com.mpmusc.model.ProviderPerformance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProviderPerformanceRepository extends JpaRepository<ProviderPerformance, Long> {
    List<ProviderPerformance> findByProviderOrderByConcurrencyAsc(String provider);

    Optional<ProviderPerformance> findByProviderAndConcurrency(String provider, Integer concurrency);
}