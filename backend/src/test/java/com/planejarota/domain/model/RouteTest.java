package com.planejarota.domain.model;

import com.planejarota.domain.exception.RouteCapacityExceededException;
import com.planejarota.domain.model.fleet.Vehicle;
import com.planejarota.domain.model.route.*;
import com.planejarota.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

class RouteTest {

    private Vehicle vehicle;
    private GeoCoordinate depot;

    @BeforeEach
    void setUp() {
        vehicle = Vehicle.create("ABC1234", "Fiat Fiorino", Vehicle.Type.VAN, new Weight(500));
        depot = new GeoCoordinate(-23.5505, -46.6333);
    }

    @Test
    void shouldCreateRouteInDraftStatus() {
        Route route = Route.create("Rota SP Centro", vehicle, depot, LocalDate.now(), LocalTime.of(8, 0));

        assertThat(route.getStatus()).isEqualTo(Route.Status.DRAFT);
        assertThat(route.getStops()).isEmpty();
        assertThat(route.getId()).isNotNull();
    }

    @Test
    void shouldAddStopWhenCapacityAllows() {
        Route route = Route.create("Rota Teste", vehicle, depot, LocalDate.now(), null);
        DeliveryOrder order = buildOrder(new Weight(100));

        route.addStop(order);

        assertThat(route.getStops()).hasSize(1);
    }

    @Test
    void shouldRejectStopWhenCapacityExceeded() {
        Route route = Route.create("Rota Teste", vehicle, depot, LocalDate.now(), null);
        DeliveryOrder heavyOrder = buildOrder(new Weight(600));

        assertThatThrownBy(() -> route.addStop(heavyOrder))
                .isInstanceOf(RouteCapacityExceededException.class)
                .hasMessageContaining("Capacidade do veículo excedida");
    }

    @Test
    void shouldNotAllowModificationAfterPlanning() {
        Route route = Route.create("Rota Teste", vehicle, depot, LocalDate.now(), null);
        route.addStop(buildOrder(new Weight(50)));
        route.plan();

        assertThatThrownBy(() -> route.addStop(buildOrder(new Weight(50))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldEmitDomainEventOnCreation() {
        Route route = Route.create("Rota Teste", vehicle, depot, LocalDate.now(), null);

        assertThat(route.pullDomainEvents()).hasSize(1);
        assertThat(route.pullDomainEvents()).isEmpty();
    }

    private DeliveryOrder buildOrder(Weight weight) {
        Address addr = new Address("Rua A", "123", null, "Centro", "São Paulo", "SP", "01310-100", "Brasil");
        GeoCoordinate loc = new GeoCoordinate(-23.5605, -46.6400);
        Customer customer = Customer.create("Cliente Teste", null, null, addr, loc, null, 3);
        return DeliveryOrder.create(customer, "ORD-" + System.nanoTime(), "Teste", weight,
                BigDecimal.TEN, null, LocalDate.now());
    }
}
