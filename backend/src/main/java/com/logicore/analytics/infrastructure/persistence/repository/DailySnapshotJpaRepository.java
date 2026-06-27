package com.logicore.analytics.infrastructure.persistence.repository;

import com.logicore.analytics.infrastructure.persistence.entity.DailySnapshotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailySnapshotJpaRepository extends JpaRepository<DailySnapshotJpaEntity, UUID> {

    List<DailySnapshotJpaEntity> findByOrganizationIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            UUID organizationId, LocalDate from, LocalDate to);

    Optional<DailySnapshotJpaEntity> findByOrganizationIdAndSnapshotDate(UUID organizationId, LocalDate date);
}
