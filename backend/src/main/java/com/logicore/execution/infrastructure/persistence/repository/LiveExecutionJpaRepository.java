package com.logicore.execution.infrastructure.persistence.repository;

import com.logicore.execution.infrastructure.persistence.entity.LiveExecutionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LiveExecutionJpaRepository extends JpaRepository<LiveExecutionJpaEntity, UUID> {
    List<LiveExecutionJpaEntity> findByOrganizationIdAndStatus(UUID organizationId, String status);
}
