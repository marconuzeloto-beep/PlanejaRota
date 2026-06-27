package com.logicore.analytics.domain.repository;

import com.logicore.analytics.domain.model.DailySnapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailySnapshotRepository {
    List<DailySnapshot> findByOrganizationAndDateRange(UUID organizationId, LocalDate from, LocalDate to);
    Optional<DailySnapshot> findByOrganizationAndDate(UUID organizationId, LocalDate date);
    DailySnapshot save(DailySnapshot snapshot);
    void upsert(DailySnapshot snapshot);
}
