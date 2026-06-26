package com.logicore.simulation.domain.valueobject;

import com.logicore.planning.domain.strategy.StrategyType;
import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.RouteMetrics;

/**
 * Resumo de um cenário dentro de uma comparação — identificador + métricas + resultado completo.
 */
public record ScenarioSummary(
        String strategyIdentifier,
        StrategyType strategyType,
        String strategyDescription,
        RouteMetrics metrics,
        DecisionResult decisionResult
) {
    public boolean isFeasible()      { return metrics.feasible(); }
    public double totalDistanceKm()  { return metrics.totalDistanceKm(); }
    public int totalTimeMinutes()    { return metrics.totalEstimatedTimeMinutes(); }
}
