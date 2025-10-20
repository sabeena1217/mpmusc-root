package com.mpmusc.repository;

import com.mpmusc.model.ProviderAvgRtt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProviderAvgRttRepository extends JpaRepository<ProviderAvgRtt, Long> {

    @Query("SELECT p.provider, p.averageRtt " +
            "FROM ProviderAvgRtt p " +
            "WHERE p.concurrency = :concurrency " +
            "AND p.recordedAt = (" +
            "   SELECT MAX(p2.recordedAt) " +
            "   FROM ProviderAvgRtt p2 " +
            "   WHERE p2.provider = p.provider " +
            "     AND p2.concurrency = :concurrency" +
            ")")
    List<Object[]> findLatestAveragesByConcurrency(@Param("concurrency") int concurrency);

}


