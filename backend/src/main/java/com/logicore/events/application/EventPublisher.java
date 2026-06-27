package com.logicore.events.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logicore.events.domain.model.LogiCoreEvent;
import com.logicore.events.infrastructure.persistence.entity.LogiCoreEventJpaEntity;
import com.logicore.events.infrastructure.persistence.repository.LogiCoreEventJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Publishes domain events to the event store asynchronously.
 * Uses REQUIRES_NEW to ensure event persistence is independent of the calling transaction.
 */
@Service
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final LogiCoreEventJpaRepository repository;
    private final ObjectMapper objectMapper;

    public EventPublisher(LogiCoreEventJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publish(UUID organizationId, String aggregateType, UUID aggregateId,
                        String eventType, Map<String, Object> payload, Map<String, Object> metadata) {
        try {
            LogiCoreEventJpaEntity entity = new LogiCoreEventJpaEntity();
            entity.setEventId(UUID.randomUUID());
            entity.setOrganizationId(organizationId);
            entity.setAggregateType(aggregateType);
            entity.setAggregateId(aggregateId);
            entity.setEventType(eventType);
            entity.setEventVersion(1);
            entity.setPayloadJson(objectMapper.writeValueAsString(payload));
            if (metadata != null) entity.setMetadataJson(objectMapper.writeValueAsString(metadata));
            entity.setOccurredAt(Instant.now());
            repository.save(entity);
        } catch (Exception ex) {
            log.error("Falha ao persistir evento {}: {}", eventType, ex.getMessage(), ex);
        }
    }
}
