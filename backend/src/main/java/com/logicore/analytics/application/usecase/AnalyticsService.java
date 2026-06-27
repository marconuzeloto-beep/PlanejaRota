package com.logicore.analytics.application.usecase;

import com.logicore.analytics.domain.model.AnalyticsSummary;
import com.logicore.analytics.domain.model.DailySnapshot;
import com.logicore.analytics.domain.repository.DailySnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final DailySnapshotRepository repository;

    public AnalyticsService(DailySnapshotRepository repository) {
        this.repository = repository;
    }

    public AnalyticsSummary getSummary(UUID organizationId, LocalDate from, LocalDate to) {
        List<DailySnapshot> snapshots = repository.findByOrganizationAndDateRange(organizationId, from, to);

        int totalRoutes         = snapshots.stream().mapToInt(DailySnapshot::totalRoutes).sum();
        int completedRoutes     = snapshots.stream().mapToInt(DailySnapshot::completedRoutes).sum();
        double totalDistanceKm  = snapshots.stream().mapToDouble(DailySnapshot::totalDistanceKm).sum();
        double totalDurationMin = snapshots.stream().mapToDouble(DailySnapshot::totalDurationMin).sum();
        int totalPlanned        = snapshots.stream().mapToInt(DailySnapshot::totalOrdersPlanned).sum();
        int totalDelivered      = snapshots.stream().mapToInt(DailySnapshot::totalOrdersDelivered).sum();
        int onTime              = snapshots.stream().mapToInt(DailySnapshot::onTimeDeliveries).sum();
        int violations          = snapshots.stream().mapToInt(DailySnapshot::constraintViolations).sum();
        double totalFuel        = snapshots.stream()
                .mapToDouble(s -> s.fuelConsumptionL() != null ? s.fuelConsumptionL() : 0).sum();
        double totalCo2         = snapshots.stream()
                .mapToDouble(s -> s.co2Kg() != null ? s.co2Kg() : 0).sum();
        double avgScore         = snapshots.stream()
                .filter(s -> s.avgRouteScore() != null)
                .mapToDouble(DailySnapshot::avgRouteScore)
                .average().orElse(0.0);

        return new AnalyticsSummary(
                totalRoutes, completedRoutes, totalDistanceKm,
                totalRoutes > 0 ? totalDistanceKm / totalRoutes : 0,
                avgScore,
                totalPlanned, totalDelivered,
                totalDelivered > 0 && totalPlanned > 0 ? (double) totalDelivered / totalPlanned : 0,
                totalDelivered > 0 ? (double) onTime / totalDelivered : 0,
                totalFuel, totalCo2, violations,
                snapshots
        );
    }

    public List<DailySnapshot> getDaily(UUID organizationId, LocalDate from, LocalDate to) {
        return repository.findByOrganizationAndDateRange(organizationId, from, to);
    }

    @Transactional
    public void upsertSnapshot(DailySnapshot snapshot) {
        repository.upsert(snapshot);
    }
}
