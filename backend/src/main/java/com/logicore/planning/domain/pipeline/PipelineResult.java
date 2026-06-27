package com.logicore.planning.domain.pipeline;

import com.logicore.planning.domain.constraint.ConstraintViolation;
import com.logicore.planning.domain.cost.RouteScore;
import com.logicore.planning.domain.valueobject.DecisionResult;

import java.util.List;

/**
 * Resultado completo de uma execução do OptimizationPipeline.
 * Contém: solução otimizada, score, violações e metadados de performance.
 */
public record PipelineResult(
        DecisionResult decisionResult,
        RouteScore score,
        List<ConstraintViolation> violations,
        long executionTimeMs,
        boolean twoOptApplied,
        double twoOptImprovementKm
) {
    public PipelineResult {
        violations = List.copyOf(violations);
    }

    public boolean hasCriticalViolations() {
        return violations.stream().anyMatch(ConstraintViolation::isError);
    }

    public int violationCount() { return violations.size(); }
}
