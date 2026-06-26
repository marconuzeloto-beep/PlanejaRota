package com.logicore.planning.infrastructure.strategy;

import com.logicore.core.domain.model.Vehicle;
import com.logicore.planning.domain.strategy.StrategyConfig;
import com.logicore.planning.domain.valueobject.*;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PriorityFirstStrategyTest {

    private PriorityFirstStrategy strategy;
    private Vehicle vehicle;
    private GeoCoordinate depot;
    private RouteConstraints constraints;
    private StrategyConfig config;

    @BeforeEach
    void setUp() {
        strategy = new PriorityFirstStrategy();
        vehicle = StrategyTestHelper.defaultVehicle();
        depot = StrategyTestHelper.depot();
        constraints = RouteConstraints.withDefaults(10000.0);
        config = StrategyConfig.defaults();
    }

    @Test
    void executeReturnsCorrectStopCount() {
        List<PlanningOrder> orders = List.of(
                StrategyTestHelper.planningOrder(-23.56, -46.64, 3, 10.0),
                StrategyTestHelper.planningOrder(-23.57, -46.65, 1, 10.0),
                StrategyTestHelper.planningOrder(-23.58, -46.66, 5, 10.0)
        );

        DecisionResult result = strategy.execute(orders, vehicle, depot, constraints, config);

        assertThat(result.orderedSteps()).hasSize(3);
    }

    @Test
    void highestPriorityOrderIsFirst() {
        // All orders at same distance from depot (same lat, different tiny lng)
        // Order at -23.5505 lat are close to depot, so priority decides
        PlanningOrder lowPriority = StrategyTestHelper.planningOrder(-23.5506, -46.6334, 1, 10.0);
        PlanningOrder highPriority = StrategyTestHelper.planningOrder(-23.5507, -46.6335, 5, 10.0);

        List<PlanningOrder> orders = List.of(lowPriority, highPriority);
        DecisionResult result = strategy.execute(orders, vehicle, depot, constraints, config);

        // First stop should be the high priority order (priority=5)
        DecisionStep firstStop = result.orderedSteps().get(0);
        assertThat(firstStop.priorityScore()).isEqualTo(5.0);
    }

    @Test
    void allStepsHavePriorityOverrideReason() {
        List<PlanningOrder> orders = List.of(
                StrategyTestHelper.planningOrder(-23.56, -46.64, 3, 10.0),
                StrategyTestHelper.planningOrder(-23.57, -46.65, 5, 10.0)
        );

        DecisionResult result = strategy.execute(orders, vehicle, depot, constraints, config);

        for (DecisionStep step : result.orderedSteps()) {
            assertThat(step.reason()).isNotNull();
            assertThat(step.reason().type()).isEqualTo(DecisionReason.ReasonType.PRIORITY_OVERRIDE);
        }
    }

    @Test
    void stopsAreOrderedSequentially() {
        List<PlanningOrder> orders = List.of(
                StrategyTestHelper.planningOrder(-23.56, -46.64, 3, 10.0),
                StrategyTestHelper.planningOrder(-23.57, -46.65, 5, 10.0)
        );

        DecisionResult result = strategy.execute(orders, vehicle, depot, constraints, config);

        for (int i = 0; i < result.orderedSteps().size(); i++) {
            assertThat(result.orderedSteps().get(i).position()).isEqualTo(i + 1);
        }
    }
}
