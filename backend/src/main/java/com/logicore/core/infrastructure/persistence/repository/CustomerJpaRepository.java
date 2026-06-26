package com.logicore.core.infrastructure.persistence.repository;

import com.logicore.core.infrastructure.persistence.entity.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, UUID> {
    List<CustomerJpaEntity> findByOrganizationId(UUID orgId);
    Optional<CustomerJpaEntity> findByOrganizationIdAndId(UUID orgId, UUID id);
    boolean existsByOrganizationIdAndId(UUID orgId, UUID id);
}
