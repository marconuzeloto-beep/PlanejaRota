package com.logicore.benchmark.domain.model;

import com.logicore.planning.domain.cost.RouteScore;
import com.logicore.planning.domain.pipeline.PipelineResult;
import com.logicore.planning.domain.strategy.OptimizationStrategy;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro imutável de uma execução do Planning Engine.
 * Persiste métricas comparativas para análise histórica e ranking de algoritmos.
 */
public record BenchmarkEntry(
        UUID id,
        UUID organizationId,
        String strategyIdentifier,
        String strategyType,
        int orderCount,
        long executionTimeMs,
        double totalDistanceKm,
        int totalTimeMinutes,
        double capacityUsagePct,
        int stopsWithViolations,
        boolean feasible,
        Double routeScore,
        String scoreGrade,
        boolean twoOptApplied,
        double twoOptImprovementKm,
        Long memoryBytes,
        String notes,
        Instant createdAt
) {
    public static BenchmarkEntry from(UUID organizationId, OptimizationStrategy strategy,
                                       PipelineResult result, int orderCount, Long memoryBytes) {
        RouteScore score = result.score();
        var metrics = result.decisionResult().metrics();
        return new BenchmarkEntry(
                UUID.randomUUID(),
                organizationId,
                strategy.getIdentifier(),
                strategy.getType().name(),
                orderCount,
                result.executionTimeMs(),
                metrics.totalDistanceKm(),
                metrics.totalEstimatedTimeMinutes(),
                metrics.capacityUsagePercent(),
                metrics.stopsWithTimeViolation(),
                metrics.feasible(),
                score != null ? score.totalScore() : null,
                score != null ? score.grade() : null,
                result.twoOptApplied(),
                result.twoOptImprovementKm(),
                memoryBytes,
                null,
                Instant.now()
        );
    }
}
