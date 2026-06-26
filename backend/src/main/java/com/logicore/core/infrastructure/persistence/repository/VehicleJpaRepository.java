package com.logicore.core.infrastructure.persistence.repository;

import com.logicore.core.infrastructure.persistence.entity.VehicleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleJpaRepository extends JpaRepository<VehicleJpaEntity, UUID> {
    List<VehicleJpaEntity> findByOrganizationId(UUID orgId);
    Optional<VehicleJpaEntity> findByOrganizationIdAndId(UUID orgId, UUID id);
    List<VehicleJpaEntity> findByOrganizationIdAndStatus(UUID orgId, String status);
}
