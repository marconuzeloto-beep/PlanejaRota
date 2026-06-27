package com.logicore.security.infrastructure.persistence.repository;

import com.logicore.security.infrastructure.persistence.entity.AuditLogJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogJpaEntity, UUID> {
    Page<AuditLogJpaEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId, Pageable pageable);
    Page<AuditLogJpaEntity> findByOrganizationIdAndUserIdOrderByCreatedAtDesc(UUID organizationId, UUID userId, Pageable pageable);
}
