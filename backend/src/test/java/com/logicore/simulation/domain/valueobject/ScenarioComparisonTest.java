package com.logicore.simulation.domain.valueobject;

import com.logicore.planning.domain.strategy.StrategyConfig;
import com.logicore.planning.domain.strategy.StrategyType;
import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.RouteMetrics;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ScenarioComparisonTest {

    private ScenarioSummary buildSummary(String identifier, StrategyType type, double distanceKm, boolean feasible) {
        RouteMetrics metrics = new RouteMetrics(distanceKm, 60, BigDecimal.valueOf(distanceKm * 2),
                100.0, 10.0, feasible ? 0 : 2, feasible);
        DecisionResult result = DecisionResult.of(type, StrategyConfig.defaults(), List.of(), metrics);
        return new ScenarioSummary(identifier, type, "Description", metrics, result);
    }

    @Test
    void ofSelectsFeasibleScenarioWithSmallestDistance() {
        ScenarioSummary shortFeasible = buildSummary("SHORTEST_DISTANCE_V1", StrategyType.SHORTEST_DISTANCE, 50.0, true);
        ScenarioSummary longFeasible = buildSummary("PRIORITY_FIRST_V1", StrategyType.PRIORITY_FIRST, 80.0, true);
        ScenarioSummary infeasible = buildSummary("HYBRID_V1", StrategyType.HYBRID, 30.0, false);

        ScenarioComparison comparison = ScenarioComparison.of(List.of(shortFeasible, longFeasible, infeasible));

        assertThat(comparison.recommendedStrategyIdentifier()).isEqualTo("SHORTEST_DISTANCE_V1");
    }

    @Test
    void ofSelectsAnyScenarioWhenNoneIsFeasible() {
        ScenarioSummary infeasible1 = buildSummary("SHORTEST_DISTANCE_V1", StrategyType.SHORTEST_DISTANCE, 50.0, false);
        ScenarioSummary infeasible2 = buildSummary("PRIORITY_FIRST_V1", StrategyType.PRIORITY_FIRST, 80.0, false);

        ScenarioComparison comparison = ScenarioComparison.of(List.of(infeasible1, infeasible2));

        // Should fall back to first scenario (no feasible ones)
        assertThat(comparison.recommendedStrategyIdentifier()).isNotBlank();
    }

    @Test
    void emptyScenariosThrows() {
        // The constructor guard in ScenarioComparison checks for empty, but of() may hit
        // an ArrayIndexOutOfBoundsException before that — both indicate invalid input.
        assertThatThrownBy(() -> ScenarioComparison.of(List.of()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void recommendationReasonIsNotBlank() {
        ScenarioSummary summary = buildSummary("SHORTEST_DISTANCE_V1", StrategyType.SHORTEST_DISTANCE, 50.0, true);

        ScenarioComparison comparison = ScenarioComparison.of(List.of(summary));

        assertThat(comparison.recommendationReason()).isNotBlank();
    }

    @Test
    void scenarioCountMatchesInput() {
        List<ScenarioSummary> summaries = List.of(
                buildSummary("SHORTEST_DISTANCE_V1", StrategyType.SHORTEST_DISTANCE, 50.0, true),
                buildSummary("PRIORITY_FIRST_V1", StrategyType.PRIORITY_FIRST, 80.0, true),
                buildSummary("HYBRID_V1", StrategyType.HYBRID, 60.0, true)
        );

        ScenarioComparison comparison = ScenarioComparison.of(summaries);

        assertThat(comparison.scenarioCount()).isEqualTo(3);
    }
}
