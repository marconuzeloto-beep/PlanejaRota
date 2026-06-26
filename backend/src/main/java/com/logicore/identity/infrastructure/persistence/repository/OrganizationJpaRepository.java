package com.logicore.identity.infrastructure.persistence.repository;

import com.logicore.identity.infrastructure.persistence.entity.OrganizationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationJpaRepository extends JpaRepository<OrganizationJpaEntity, UUID> {
    Optional<OrganizationJpaEntity> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
