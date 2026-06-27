package com.logicore.benchmark.infrastructure.persistence.repository;

import com.logicore.benchmark.infrastructure.persistence.entity.BenchmarkEntryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BenchmarkJpaRepository extends JpaRepository<BenchmarkEntryJpaEntity, UUID> {

    List<BenchmarkEntryJpaEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    List<BenchmarkEntryJpaEntity> findByOrganizationIdAndStrategyIdentifierOrderByCreatedAtDesc(
            UUID organizationId, String strategyIdentifier);

    @Query("""
        SELECT b.strategyIdentifier, b.strategyType,
               COUNT(b), AVG(b.executionTimeMs), MAX(b.executionTimeMs),
               AVG(b.totalDistanceKm), AVG(b.routeScore), AVG(b.capacityUsagePct),
               SUM(CASE WHEN b.feasible = TRUE THEN 1 ELSE 0 END)
        FROM BenchmarkEntryJpaEntity b
        WHERE b.organizationId = :orgId
        GROUP BY b.strategyIdentifier, b.strategyType
        ORDER BY AVG(b.routeScore) DESC NULLS LAST
    """)
    List<Object[]> aggregateByStrategy(@Param("orgId") UUID orgId);
}
