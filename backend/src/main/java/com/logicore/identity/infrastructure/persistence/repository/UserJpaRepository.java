package com.logicore.identity.infrastructure.persistence.repository;

import com.logicore.identity.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByOrganizationIdAndEmail(UUID organizationId, String email);
    Optional<UserJpaEntity> findByOrganizationIdAndId(UUID organizationId, UUID id);
    List<UserJpaEntity> findAllByOrganizationId(UUID organizationId);
    boolean existsByOrganizationIdAndEmail(UUID organizationId, String email);
}
