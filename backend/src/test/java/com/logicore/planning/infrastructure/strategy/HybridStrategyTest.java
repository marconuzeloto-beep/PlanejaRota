package com.logicore.planning.infrastructure.strategy;

import com.logicore.core.domain.model.Vehicle;
import com.logicore.planning.domain.strategy.StrategyConfig;
import com.logicore.planning.domain.valueobject.*;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class HybridStrategyTest {

    private HybridStrategy strategy;
    private Vehicle vehicle;
    private GeoCoordinate depot;
    private RouteConstraints constraints;

    @BeforeEach
    void setUp() {
        strategy = new HybridStrategy();
        vehicle = StrategyTestHelper.defaultVehicle();
        depot = StrategyTestHelper.depot();
        constraints = RouteConstraints.withDefaults(10000.0);
    }

    @Test
    void executeReturnsCorrectStopCount() {
        List<PlanningOrder> orders = List.of(
                StrategyTestHelper.planningOrder(-23.56, -46.64, 3, 10.0),
                StrategyTestHelper.planningOrder(-23.57, -46.65, 5, 10.0),
                StrategyTestHelper.planningOrder(-23.58, -46.66, 1, 10.0)
        );

        DecisionResult result = strategy.execute(orders, vehicle, depot, constraints, StrategyConfig.defaults());

        assertThat(result.orderedSteps()).hasSize(3);
    }

    @Test
    void allStepsHaveHybridScoreReason() {
        List<PlanningOrder> orders = List.of(
                StrategyTestHelper.planningOrder(-23.56, -46.64, 3, 10.0),
                StrategyTestHelper.planningOrder(-23.57, -46.65, 5, 10.0)
        );

        DecisionResult result = strategy.execute(orders, vehicle, depot, constraints, StrategyConfig.defaults());

        for (DecisionStep step : result.orderedSteps()) {
            assertThat(step.reason()).isNotNull();
            assertThat(step.reason().type()).isEqualTo(DecisionReason.ReasonType.HYBRID_SCORE);
        }
    }

    @Test
    void withDistanceWeight1AndPriorityWeight0BehavesLikeNearestNeighbor() {
        // When distance weight is 1.0 and priority weight 0.0,
        // it should pick nearest neighbor regardless of priority
        StrategyConfig distanceOnly = StrategyConfig.hybrid(1.0, 0.0);
        ShortestDistanceStrategy nearestNeighbor = new ShortestDistanceStrategy();
        StrategyConfig defaultConfig = StrategyConfig.defaults();

        // Same orders for both strategies
        PlanningOrder close = StrategyTestHelper.planningOrder(-23.551, -46.634, 1, 10.0); // very close to depot, low priority
        PlanningOrder farHighPrio = StrategyTestHelper.planningOrder(-23.60, -46.70, 5, 10.0); // far, high priority

        List<PlanningOrder> orders = List.of(close, farHighPrio);

        DecisionResult hybridResult = strategy.execute(orders, vehicle, depot, constraints, distanceOnly);
        DecisionResult nnResult = nearestNeighbor.execute(orders, vehicle, depot, constraints, defaultConfig);

        // Both should pick the same first stop (nearest neighbor)
        assertThat(hybridResult.orderedSteps().get(0).location())
                .isEqualTo(nnResult.orderedSteps().get(0).location());
    }

    @Test
    void stopsAreOrderedSequentially() {
        List<PlanningOrder> orders = List.of(
                StrategyTestHelper.planningOrder(-23.56, -46.64, 3, 10.0),
                StrategyTestHelper.planningOrder(-23.57, -46.65, 5, 10.0)
        );

        DecisionResult result = strategy.execute(orders, vehicle, depot, constraints, StrategyConfig.hybrid(0.6, 0.4));

        for (int i = 0; i < result.orderedSteps().size(); i++) {
            assertThat(result.orderedSteps().get(i).position()).isEqualTo(i + 1);
        }
    }
}
