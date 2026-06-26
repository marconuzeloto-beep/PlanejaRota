package com.logicore.planning.infrastructure.strategy;

import com.logicore.core.domain.model.*;
import com.logicore.planning.domain.valueobject.PlanningOrder;
import com.logicore.shared.domain.valueobject.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Test helper — shared factory methods for strategy tests.
 */
class StrategyTestHelper {

    static Vehicle defaultVehicle() {
        return Vehicle.create(
                UUID.randomUUID(), "TEST-001", "Test Van",
                VehicleType.VAN, new Weight(10000.0), Money.brl(2.0)
        );
    }

    static GeoCoordinate depot() {
        return new GeoCoordinate(-23.5505, -46.6333);
    }

    static PlanningOrder planningOrder(double lat, double lng, int priorityValue, double weightKg) {
        Address address = new Address("Rua Test", "1", null, "Centro", "SP", "SP", "01001-000", "Brasil");
        GeoCoordinate location = new GeoCoordinate(lat, lng);
        TimeWindow window = new TimeWindow(LocalTime.of(8, 0), LocalTime.of(22, 0));
        Priority priority = new Priority(priorityValue);

        Customer customer = Customer.create(UUID.randomUUID(), "Cliente", null, null, address, location, window, priority);
        DeliveryOrder order = DeliveryOrder.create(
                UUID.randomUUID(), customer.getId(), "ORD-" + UUID.randomUUID(),
                "Desc", new Weight(weightKg), Money.brl(100.0),
                LocalDate.now().plusDays(1), window
        );
        return PlanningOrder.from(order, customer);
    }
}
