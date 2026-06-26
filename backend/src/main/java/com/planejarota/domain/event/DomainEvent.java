package com.planejarota.domain.event;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredAt();
}
