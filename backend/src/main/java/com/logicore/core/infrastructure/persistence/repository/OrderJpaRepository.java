package com.logicore.core.infrastructure.persistence.repository;

import com.logicore.core.infrastructure.persistence.entity.DeliveryOrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<DeliveryOrderJpaEntity, UUID> {
    List<DeliveryOrderJpaEntity> findByOrganizationId(UUID orgId);
    Optional<DeliveryOrderJpaEntity> findByOrganizationIdAndId(UUID orgId, UUID id);
    List<DeliveryOrderJpaEntity> findByOrganizationIdAndDeliveryDateAndStatus(UUID orgId, LocalDate date, String status);
    boolean existsByOrganizationIdAndOrderCode(UUID orgId, String code);
}
