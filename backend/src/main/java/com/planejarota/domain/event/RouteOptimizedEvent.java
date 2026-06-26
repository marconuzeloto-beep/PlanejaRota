package com.planejarota.domain.event;

import java.time.Instant;
import java.util.UUID;

public record RouteOptimizedEvent(
        UUID routeId,
        double totalDistanceKm,
        Instant occurredAt
) implements DomainEvent {

    public RouteOptimizedEvent(UUID routeId, double totalDistanceKm) {
        this(routeId, totalDistanceKm, Instant.now());
    }
}
