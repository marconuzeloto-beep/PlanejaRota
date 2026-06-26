package com.planejarota.domain.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RouteCreatedEvent(
        UUID routeId,
        String routeName,
        LocalDate scheduledDate,
        Instant occurredAt
) implements DomainEvent {

    public RouteCreatedEvent(UUID routeId, String routeName, LocalDate scheduledDate) {
        this(routeId, routeName, scheduledDate, Instant.now());
    }
}
