package com.logicore.benchmark.domain.repository;

import com.logicore.benchmark.domain.model.BenchmarkEntry;

import java.util.List;
import java.util.UUID;

public interface BenchmarkRepository {
    BenchmarkEntry save(BenchmarkEntry entry);
    List<BenchmarkEntry> findByOrganization(UUID organizationId, int limit);
    List<BenchmarkEntry> findByOrganizationAndStrategy(UUID organizationId, String strategyIdentifier, int limit);
    List<BenchmarkSummary> aggregateByStrategy(UUID organizationId);

    record BenchmarkSummary(
            String strategyIdentifier,
            String strategyType,
            long executionCount,
            double avgExecutionTimeMs,
            double maxExecutionTimeMs,
            double avgDistanceKm,
            double avgScore,
            double avgCapacityUsagePct,
            long feasibleCount
    ) {}
}
