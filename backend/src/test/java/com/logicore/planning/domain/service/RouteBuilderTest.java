package com.logicore.planning.domain.service;

import com.logicore.core.domain.model.*;
import com.logicore.planning.domain.strategy.StrategyConfig;
import com.logicore.planning.domain.valueobject.*;
import com.logicore.planning.infrastructure.strategy.ShortestDistanceStrategy;
import com.logicore.shared.domain.exception.BusinessRuleException;
import com.logicore.shared.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class RouteBuilderTest {

    private Vehicle vehicle;
    private GeoCoordinate depot;
    private RouteConstraints constraints;
    private ShortestDistanceStrategy strategy;
    private StrategyConfig config;

    @BeforeEach
    void setUp() {
        vehicle = Vehicle.create(
                UUID.randomUUID(), "ABC-1234", "Fiat Ducato",
                VehicleType.VAN, new Weight(10000.0), Money.brl(2.50)
        );
        depot = new GeoCoordinate(-23.5505, -46.6333);
        constraints = RouteConstraints.withDefaults(10000.0);
        strategy = new ShortestDistanceStrategy();
        config = StrategyConfig.defaults();
    }

    private PlanningOrder createOrder(double lat, double lng, double weightKg) {
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Address address = new Address("Rua Test", "1", null, "Centro", "São Paulo", "SP", "01001-000", "Brasil");
        GeoCoordinate location = new GeoCoordinate(lat, lng);
        TimeWindow window = new TimeWindow(LocalTime.of(8, 0), LocalTime.of(18, 0));
        Priority priority = new Priority(3);

        Customer customer = Customer.create(UUID.randomUUID(), "Cliente", "a@b.com", null, address, location, window, priority);
        // Override customer id with orderId for matching
        DeliveryOrder order = DeliveryOrder.create(
                UUID.randomUUID(), customer.getId(), "ORD-" + orderId,
                "Desc", new Weight(weightKg), Money.brl(100.0),
                LocalDate.now().plusDays(1), window
        );
        return PlanningOrder.from(order, customer);
    }

    @Test
    void buildWithValidInputsReturnsNonNullResult() {
        List<PlanningOrder> orders = List.of(createOrder(-23.56, -46.64, 100.0));
        DecisionResult result = RouteBuilder.build(orders, vehicle, depot, constraints, strategy, config);
        assertThat(result).isNotNull();
    }

    @Test
    void buildWithEmptyOrdersThrows() {
        assertThatThrownBy(() -> RouteBuilder.build(List.of(), vehicle, depot, constraints, strategy, config))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void buildWithNullVehicleThrows() {
        List<PlanningOrder> orders = List.of(createOrder(-23.56, -46.64, 100.0));
        assertThatThrownBy(() -> RouteBuilder.build(orders, null, depot, constraints, strategy, config))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void buildWithOverweightThrows() {
        // capacity is 10000 kg, but constraints says 100 kg max
        RouteConstraints lightConstraints = RouteConstraints.withDefaults(100.0);
        List<PlanningOrder> orders = List.of(
                createOrder(-23.56, -46.64, 60.0),
                createOrder(-23.57, -46.65, 60.0)  // total 120 > 100
        );
        assertThatThrownBy(() -> RouteBuilder.build(orders, vehicle, depot, lightConstraints, strategy, config))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void buildReturnsResultWithSameNumberOfStepsAsOrders() {
        List<PlanningOrder> orders = List.of(
                createOrder(-23.56, -46.64, 50.0),
                createOrder(-23.57, -46.65, 50.0),
                createOrder(-23.58, -46.66, 50.0)
        );
        DecisionResult result = RouteBuilder.build(orders, vehicle, depot, constraints, strategy, config);
        assertThat(result.stopCount()).isEqualTo(3);
        assertThat(result.orderedSteps()).hasSize(3);
    }

    @Test
    void buildWithUnavailableVehicleThrows() {
        vehicle.markInUse();
        List<PlanningOrder> orders = List.of(createOrder(-23.56, -46.64, 100.0));
        assertThatThrownBy(() -> RouteBuilder.build(orders, vehicle, depot, constraints, strategy, config))
                .isInstanceOf(BusinessRuleException.class);
    }
}
