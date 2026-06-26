package com.logicore.simulation.domain.valueobject;

import java.util.Comparator;
import java.util.List;

/**
 * Resultado da simulação comparativa: N cenários com suas métricas e DecisionResults.
 * Imutável — produzido pelo SimulationEngine, nunca modificado.
 */
public record ScenarioComparison(
        List<ScenarioSummary> scenarios,
        String recommendedStrategyIdentifier,
        String recommendationReason
) {
    public ScenarioComparison {
        if (scenarios == null || scenarios.isEmpty())
            throw new IllegalArgumentException("Scenarios não podem ser vazios");
        scenarios = List.copyOf(scenarios);
    }

    public static ScenarioComparison of(List<ScenarioSummary> scenarios) {
        ScenarioSummary best = scenarios.stream()
                .filter(ScenarioSummary::isFeasible)
                .min(Comparator.comparingDouble(ScenarioSummary::totalDistanceKm))
                .orElse(scenarios.get(0));

        String reason = best.isFeasible()
                ? String.format("Menor distância total entre os cenários viáveis: %.1f km", best.totalDistanceKm())
                : "Nenhum cenário foi totalmente viável; selecionado o de menor distância";

        return new ScenarioComparison(scenarios, best.strategyIdentifier(), reason);
    }

    public int scenarioCount() { return scenarios.size(); }
}
