package com.logicore.security.infrastructure.persistence.repository;

import com.logicore.security.infrastructure.persistence.entity.ApiKeyJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyJpaRepository extends JpaRepository<ApiKeyJpaEntity, UUID> {
    Optional<ApiKeyJpaEntity> findByKeyHash(String keyHash);
    List<ApiKeyJpaEntity> findByOrganizationIdAndActiveTrue(UUID organizationId);

    @Modifying
    @Query("UPDATE ApiKeyJpaEntity k SET k.lastUsedAt = :now WHERE k.id = :id")
    void updateLastUsed(UUID id, Instant now);
}
