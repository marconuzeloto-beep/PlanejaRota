package com.logicore.planning.domain.model;

import com.logicore.core.domain.model.Customer;
import com.logicore.core.domain.model.DeliveryOrder;
import com.logicore.core.domain.model.Vehicle;
import com.logicore.core.domain.model.VehicleType;
import com.logicore.planning.domain.strategy.StrategyConfig;
import com.logicore.planning.domain.strategy.StrategyType;
import com.logicore.planning.domain.valueobject.*;
import com.logicore.shared.domain.exception.BusinessRuleException;
import com.logicore.shared.domain.valueobject.*;
import com.logicore.planning.infrastructure.strategy.ShortestDistanceStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class RouteTest {

    private UUID orgId;
    private UUID vehicleId;
    private GeoCoordinate depot;
    private RouteConstraints constraints;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        vehicleId = UUID.randomUUID();
        depot = new GeoCoordinate(-23.5505, -46.6333);
        constraints = RouteConstraints.withDefaults(1000.0);
    }

    @Test
    void createStartsAsDraft() {
        Route route = Route.create(orgId, vehicleId, depot, LocalDate.now().plusDays(1), constraints);
        assertThat(route.getStatus()).isEqualTo(RouteStatus.DRAFT);
        assertThat(route.getStops()).isEmpty();
        assertThat(route.getDecisionResult()).isNull();
    }

    @Test
    void applyDecisionResultTransitionsToPlanedAndCreatesStops() {
        Route route = Route.create(orgId, vehicleId, depot, LocalDate.now().plusDays(1), constraints);

        DecisionResult result = buildDecisionResult(1);
        route.applyDecisionResult(result);

        assertThat(route.getStatus()).isEqualTo(RouteStatus.PLANNED);
        assertThat(route.getStops()).hasSize(1);
        assertThat(route.getDecisionResult()).isEqualTo(result);
    }

    @Test
    void applyDecisionResultOnNonDraftThrows() {
        Route route = Route.create(orgId, vehicleId, depot, LocalDate.now().plusDays(1), constraints);
        route.applyDecisionResult(buildDecisionResult(1));

        assertThatThrownBy(() -> route.applyDecisionResult(buildDecisionResult(1)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void startExecutionRequiresPlanned() {
        Route route = Route.create(orgId, vehicleId, depot, LocalDate.now().plusDays(1), constraints);
        assertThatThrownBy(route::startExecution)
                .isInstanceOf(BusinessRuleException.class);

        route.applyDecisionResult(buildDecisionResult(1));
        route.startExecution();
        assertThat(route.getStatus()).isEqualTo(RouteStatus.IN_PROGRESS);
    }

    @Test
    void completeRequiresInProgress() {
        Route route = Route.create(orgId, vehicleId, depot, LocalDate.now().plusDays(1), constraints);
        route.applyDecisionResult(buildDecisionResult(1));

        assertThatThrownBy(route::complete)
                .isInstanceOf(BusinessRuleException.class);

        route.startExecution();
        route.complete();
        assertThat(route.getStatus()).isEqualTo(RouteStatus.COMPLETED);
    }

    @Test
    void cancelOnCompletedThrows() {
        Route route = Route.create(orgId, vehicleId, depot, LocalDate.now().plusDays(1), constraints);
        route.applyDecisionResult(buildDecisionResult(1));
        route.startExecution();
        route.complete();

        assertThatThrownBy(route::cancel)
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void cancelOnDraftSucceeds() {
        Route route = Route.create(orgId, vehicleId, depot, LocalDate.now().plusDays(1), constraints);
        route.cancel();
        assertThat(route.getStatus()).isEqualTo(RouteStatus.CANCELLED);
    }

    private DecisionResult buildDecisionResult(int orderCount) {
        GeoCoordinate location = new GeoCoordinate(-23.56, -46.64);
        List<DecisionStep> steps = new java.util.ArrayList<>();
        for (int i = 1; i <= orderCount; i++) {
            steps.add(DecisionStep.nearestNeighbor(
                    i, UUID.randomUUID(), UUID.randomUUID(), "Customer " + i,
                    location, 1.0, (double) i, LocalTime.of(9, 0), true, null
            ));
        }
        RouteMetrics metrics = new RouteMetrics((double) orderCount, orderCount * 5,
                BigDecimal.valueOf(orderCount * 2.5), 50.0 * orderCount, 5.0 * orderCount, 0, true);
        return DecisionResult.of(StrategyType.SHORTEST_DISTANCE, StrategyConfig.defaults(), steps, metrics);
    }
}
