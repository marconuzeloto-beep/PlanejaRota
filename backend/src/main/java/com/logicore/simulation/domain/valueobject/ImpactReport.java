package com.logicore.simulation.domain.valueobject;

import com.logicore.planning.domain.valueobject.DecisionResult;

import java.util.UUID;

/**
 * Relatório de impacto de uma operação What-If (ex: adicionar um cliente a uma rota existente).
 */
public record ImpactReport(
        UUID simulatedForOrderId,
        DecisionResult originalResult,
        DecisionResult newResult,
        double additionalDistanceKm,
        int additionalTimeMinutes,
        boolean stillFeasible
) {
    public static ImpactReport of(UUID orderId, DecisionResult original, DecisionResult updated) {
        double addDist = updated.metrics().totalDistanceKm() - original.metrics().totalDistanceKm();
        int addTime   = updated.metrics().totalEstimatedTimeMinutes() - original.metrics().totalEstimatedTimeMinutes();
        return new ImpactReport(orderId, original, updated, addDist, addTime, updated.metrics().feasible());
    }
}
