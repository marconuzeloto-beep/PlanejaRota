package com.logicore.benchmark.infrastructure.persistence.repository;

import com.logicore.benchmark.domain.model.BenchmarkEntry;
import com.logicore.benchmark.domain.repository.BenchmarkRepository;
import com.logicore.benchmark.infrastructure.persistence.entity.BenchmarkEntryJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class BenchmarkRepositoryAdapter implements BenchmarkRepository {

    private final BenchmarkJpaRepository jpa;

    public BenchmarkRepositoryAdapter(BenchmarkJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public BenchmarkEntry save(BenchmarkEntry entry) {
        jpa.save(toEntity(entry));
        return entry;
    }

    @Override
    public List<BenchmarkEntry> findByOrganization(UUID organizationId, int limit) {
        return jpa.findByOrganizationIdOrderByCreatedAtDesc(organizationId)
                .stream().limit(limit).map(this::toDomain).toList();
    }

    @Override
    public List<BenchmarkEntry> findByOrganizationAndStrategy(UUID organizationId,
                                                               String strategyIdentifier, int limit) {
        return jpa.findByOrganizationIdAndStrategyIdentifierOrderByCreatedAtDesc(organizationId, strategyIdentifier)
                .stream().limit(limit).map(this::toDomain).toList();
    }

    @Override
    public List<BenchmarkSummary> aggregateByStrategy(UUID organizationId) {
        return jpa.aggregateByStrategy(organizationId).stream().map(row -> new BenchmarkSummary(
                (String) row[0],
                (String) row[1],
                ((Number) row[2]).longValue(),
                ((Number) row[3]).doubleValue(),
                ((Number) row[4]).doubleValue(),
                ((Number) row[5]).doubleValue(),
                row[6] != null ? ((Number) row[6]).doubleValue() : 0.0,
                ((Number) row[7]).doubleValue(),
                ((Number) row[8]).longValue()
        )).toList();
    }

    private BenchmarkEntryJpaEntity toEntity(BenchmarkEntry e) {
        var entity = new BenchmarkEntryJpaEntity();
        entity.setId(e.id());
        entity.setOrganizationId(e.organizationId());
        entity.setStrategyIdentifier(e.strategyIdentifier());
        entity.setStrategyType(e.strategyType());
        entity.setOrderCount(e.orderCount());
        entity.setExecutionTimeMs(e.executionTimeMs());
        entity.setTotalDistanceKm(e.totalDistanceKm());
        entity.setTotalTimeMinutes(e.totalTimeMinutes());
        entity.setCapacityUsagePct(e.capacityUsagePct());
        entity.setStopsWithViolations(e.stopsWithViolations());
        entity.setFeasible(e.feasible());
        entity.setRouteScore(e.routeScore());
        entity.setScoreGrade(e.scoreGrade());
        entity.setTwoOptApplied(e.twoOptApplied());
        entity.setTwoOptImprovementKm(e.twoOptImprovementKm());
        entity.setMemoryBytes(e.memoryBytes());
        entity.setNotes(e.notes());
        entity.setCreatedAt(e.createdAt());
        return entity;
    }

    private BenchmarkEntry toDomain(BenchmarkEntryJpaEntity e) {
        return new BenchmarkEntry(e.getId(), e.getOrganizationId(), e.getStrategyIdentifier(),
                e.getStrategyType(), e.getOrderCount(), e.getExecutionTimeMs(),
                e.getTotalDistanceKm(), e.getTotalTimeMinutes(), e.getCapacityUsagePct(),
                e.getStopsWithViolations(), e.isFeasible(), e.getRouteScore(), e.getScoreGrade(),
                e.isTwoOptApplied(), e.getTwoOptImprovementKm(), e.getMemoryBytes(),
                e.getNotes(), e.getCreatedAt());
    }
}
