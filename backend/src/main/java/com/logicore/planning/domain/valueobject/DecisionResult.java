package com.logicore.planning.domain.valueobject;

import com.logicore.planning.domain.strategy.StrategyConfig;
import com.logicore.planning.domain.strategy.StrategyType;

import java.time.Instant;
import java.util.List;

/**
 * Saída OBRIGATÓRIA de toda execução do Planning Engine.
 * Nenhuma estratégia pode retornar null ou um resultado sem DecisionStep para cada parada.
 *
 * Invariante: orderedSteps.size() == número de pedidos na entrada.
 */
public record DecisionResult(
        StrategyType strategyType,
        StrategyConfig strategyConfig,
        List<DecisionStep> orderedSteps,
        RouteMetrics metrics,
        Instant generatedAt
) {
    public DecisionResult {
        if (strategyType == null) throw new IllegalArgumentException("StrategyType é obrigatório");
        if (orderedSteps == null) throw new IllegalArgumentException("DecisionSteps são obrigatórios");
        if (metrics == null) throw new IllegalArgumentException("RouteMetrics são obrigatórias");
        orderedSteps = List.copyOf(orderedSteps); // defensiva — imutável
        generatedAt  = generatedAt != null ? generatedAt : Instant.now();
    }

    public static DecisionResult of(StrategyType type, StrategyConfig config,
                                     List<DecisionStep> steps, RouteMetrics metrics) {
        return new DecisionResult(type, config, steps, metrics, Instant.now());
    }

    public int stopCount() { return orderedSteps.size(); }

    public boolean isFullyFeasible() { return metrics.feasible() && metrics.stopsWithTimeViolation() == 0; }
}
