package com.logicore.analytics.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public record DailySnapshot(
        UUID id,
        UUID organizationId,
        LocalDate snapshotDate,
        int totalRoutes,
        int completedRoutes,
        double totalDistanceKm,
        double totalDurationMin,
        Double avgRouteScore,
        int totalOrdersPlanned,
        int totalOrdersDelivered,
        Double fuelConsumptionL,
        Double co2Kg,
        int onTimeDeliveries,
        int constraintViolations
) {
    public double completionRate() {
        return totalRoutes == 0 ? 0.0 : (double) completedRoutes / totalRoutes;
    }

    public double onTimeRate() {
        return totalOrdersDelivered == 0 ? 0.0 : (double) onTimeDeliveries / totalOrdersDelivered;
    }
}
