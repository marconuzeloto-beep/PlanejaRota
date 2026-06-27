package com.logicore.events.infrastructure.persistence.repository;

import com.logicore.events.infrastructure.persistence.entity.LogiCoreEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LogiCoreEventJpaRepository extends JpaRepository<LogiCoreEventJpaEntity, Long> {
    List<LogiCoreEventJpaEntity> findByAggregateTypeAndAggregateIdOrderByIdAsc(String aggregateType, UUID aggregateId);
    List<LogiCoreEventJpaEntity> findByOrganizationIdAndEventTypeOrderByIdAsc(UUID organizationId, String eventType);
}
