package com.logicore.planning.infrastructure.persistence.repository;

import com.logicore.planning.infrastructure.persistence.entity.RouteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RouteJpaRepository extends JpaRepository<RouteJpaEntity, UUID> {

    Optional<RouteJpaEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);

    List<RouteJpaEntity> findByOrganizationId(UUID organizationId);

    List<RouteJpaEntity> findByOrganizationIdAndPlannedDate(UUID organizationId, LocalDate date);

    List<RouteJpaEntity> findByOrganizationIdAndStatus(UUID organizationId, String status);

    boolean existsByIdAndOrganizationId(UUID id, UUID organizationId);
}
