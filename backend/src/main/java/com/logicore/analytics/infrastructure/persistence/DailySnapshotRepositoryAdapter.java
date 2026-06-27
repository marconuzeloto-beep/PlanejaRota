package com.logicore.analytics.infrastructure.persistence;

import com.logicore.analytics.domain.model.DailySnapshot;
import com.logicore.analytics.domain.repository.DailySnapshotRepository;
import com.logicore.analytics.infrastructure.persistence.entity.DailySnapshotJpaEntity;
import com.logicore.analytics.infrastructure.persistence.repository.DailySnapshotJpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DailySnapshotRepositoryAdapter implements DailySnapshotRepository {

    private final DailySnapshotJpaRepository jpa;

    public DailySnapshotRepositoryAdapter(DailySnapshotJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<DailySnapshot> findByOrganizationAndDateRange(UUID organizationId, LocalDate from, LocalDate to) {
        return jpa.findByOrganizationIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(organizationId, from, to)
                  .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<DailySnapshot> findByOrganizationAndDate(UUID organizationId, LocalDate date) {
        return jpa.findByOrganizationIdAndSnapshotDate(organizationId, date).map(this::toDomain);
    }

    @Override
    public DailySnapshot save(DailySnapshot snapshot) {
        return toDomain(jpa.save(toEntity(snapshot)));
    }

    @Override
    public void upsert(DailySnapshot snapshot) {
        var existing = jpa.findByOrganizationIdAndSnapshotDate(
                snapshot.organizationId(), snapshot.snapshotDate());
        DailySnapshotJpaEntity entity = existing.orElseGet(DailySnapshotJpaEntity::new);
        fillEntity(entity, snapshot);
        if (entity.getCreatedAt() == null) entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        jpa.save(entity);
    }

    private DailySnapshot toDomain(DailySnapshotJpaEntity e) {
        return new DailySnapshot(
                e.getId(), e.getOrganizationId(), e.getSnapshotDate(),
                e.getTotalRoutes(), e.getCompletedRoutes(),
                e.getTotalDistanceKm() != null ? e.getTotalDistanceKm().doubleValue() : 0,
                e.getTotalDurationMin() != null ? e.getTotalDurationMin().doubleValue() : 0,
                e.getAvgRouteScore() != null ? e.getAvgRouteScore().doubleValue() : null,
                e.getTotalOrdersPlanned(), e.getTotalOrdersDelivered(),
                e.getFuelConsumptionL() != null ? e.getFuelConsumptionL().doubleValue() : null,
                e.getCo2Kg() != null ? e.getCo2Kg().doubleValue() : null,
                e.getOnTimeDeliveries(), e.getConstraintViolations()
        );
    }

    private DailySnapshotJpaEntity toEntity(DailySnapshot s) {
        DailySnapshotJpaEntity e = new DailySnapshotJpaEntity();
        fillEntity(e, s);
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        return e;
    }

    private void fillEntity(DailySnapshotJpaEntity e, DailySnapshot s) {
        e.setOrganizationId(s.organizationId());
        e.setSnapshotDate(s.snapshotDate());
        e.setTotalRoutes(s.totalRoutes());
        e.setCompletedRoutes(s.completedRoutes());
        e.setTotalDistanceKm(BigDecimal.valueOf(s.totalDistanceKm()));
        e.setTotalDurationMin(BigDecimal.valueOf(s.totalDurationMin()));
        if (s.avgRouteScore() != null) e.setAvgRouteScore(BigDecimal.valueOf(s.avgRouteScore()));
        e.setTotalOrdersPlanned(s.totalOrdersPlanned());
        e.setTotalOrdersDelivered(s.totalOrdersDelivered());
        if (s.fuelConsumptionL() != null) e.setFuelConsumptionL(BigDecimal.valueOf(s.fuelConsumptionL()));
        if (s.co2Kg() != null) e.setCo2Kg(BigDecimal.valueOf(s.co2Kg()));
        e.setOnTimeDeliveries(s.onTimeDeliveries());
        e.setConstraintViolations(s.constraintViolations());
    }
}
