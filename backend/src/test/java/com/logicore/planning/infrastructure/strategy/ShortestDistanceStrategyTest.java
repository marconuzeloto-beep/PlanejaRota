package com.logicore.planning.infrastructure.strategy;

import com.logicore.core.domain.model.Vehicle;
import com.logicore.planning.domain.strategy.StrategyConfig;
import com.logicore.planning.domain.valueobject.*;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ShortestDistanceStrategyTest {

    private ShortestDistanceStrategy strategy;
    private Vehicle vehicle;
    private GeoCoordinate depot;
    private RouteConstraints constraints;
    private StrategyConfig config;

    @BeforeEach
    void setUp() {
        strategy = new ShortestDistanceStrategy();
        vehicle = StrategyTestHelper.defaultVehicle();
        depot = StrategyTestHelper.depot();
        constraints = RouteConstraints.withDefaults(10000.0);
        config = StrategyConfig.defaults();
    }

    @Test
    void executeReturnsDecisionResultWithCorrectStopCount() {
        List<PlanningOrder> orders = List.of(
                StrategyTestHelper.planningOrder(-23.56, -46.64, 3, 10.0),
                StrategyTestHelper.planningOrder(-23.57, -46.65, 3, 10.0),
                StrategyTestHelper.planningOrder(-23.58, -46.66, 3, 10.0)
        );

        DecisionResult result = strategy.execute(orders, vehicle, depot, constraints, config);

        assertThat(result).isNotNull();
        assertThat(result.orderedSteps()).hasSize(3);
    }

    @Test
    void stopsAreOrderedSequentially() {
        List<PlanningOrder> orders = List.of(
                StrategyTestHelper.planningOrder(-23.56, -46.64, 3, 10.0),
                StrategyTestHelper.planningOrder(-23.57, -46.65, 3, 10.0)
        );

        DecisionResult result = strategy.execute(orders, vehicle, depot, constraints, config);

        for (int i = 0; i < result.orderedSteps().size(); i++) {
            assertThat(result.orderedSteps().get(i).position()).isEqualTo(i + 1);
        }
    }

    @Test
    void cumulativeDistanceIncreasesMonotonically() {
        List<PlanningOrder> orders = List.of(
                StrategyTestHelper.planningOrder(-23.56, -46.64, 3, 10.0),
                StrategyTestHelper.planningOrder(-23.57, -46.65, 3, 10.0),
                StrategyTestHelper.planningOrder(-23.58, -46.66, 3, 10.0)
        );

        DecisionResult result = strategy.execute(orders, vehicle, depot, constraints, config);

        double prev = 0.0;
        for (DecisionStep step : result.orderedSteps()) {
            assertThat(step.cumulativeDistanceKm()).isGreaterThanOrEqualTo(prev);
            prev = step.cumulativeDistanceKm();
        }
    }

    @Test
    void allStepsHaveNearestNeighborReason() {
        List<PlanningOrder> orders = List.of(
                StrategyTestHelper.planningOrder(-23.56, -46.64, 3, 10.0),
                StrategyTestHelper.planningOrder(-23.57, -46.65, 3, 10.0)
        );

        DecisionResult result = strategy.execute(orders, vehicle, depot, constraints, config);

        for (DecisionStep step : result.orderedSteps()) {
            assertThat(step.reason()).isNotNull();
            assertThat(step.reason().type()).isEqualTo(DecisionReason.ReasonType.NEAREST_NEIGHBOR);
        }
    }

    @Test
    void singleOrderWorksCorrectly() {
        List<PlanningOrder> orders = List.of(
                StrategyTestHelper.planningOrder(-23.56, -46.64, 3, 10.0)
        );

        DecisionResult result = strategy.execute(orders, vehicle, depot, constraints, config);

        assertThat(result.stopCount()).isEqualTo(1);
        assertThat(result.orderedSteps().get(0).position()).isEqualTo(1);
    }

    @Test
    void totalDistanceKmIsPositive() {
        List<PlanningOrder> orders = List.of(
                StrategyTestHelper.planningOrder(-23.56, -46.64, 3, 10.0),
                StrategyTestHelper.planningOrder(-23.57, -46.65, 3, 10.0)
        );

        DecisionResult result = strategy.execute(orders, vehicle, depot, constraints, config);

        assertThat(result.metrics().totalDistanceKm()).isGreaterThan(0.0);
    }
}
