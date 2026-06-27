package com.logicore.execution.application.usecase;

import com.logicore.execution.infrastructure.persistence.entity.LiveExecutionJpaEntity;
import com.logicore.execution.infrastructure.persistence.repository.LiveExecutionJpaRepository;
import com.logicore.execution.infrastructure.websocket.LiveMapWebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LiveExecutionService {

    private final LiveExecutionJpaRepository repository;
    private final LiveMapWebSocketHandler webSocketHandler;

    public LiveExecutionService(LiveExecutionJpaRepository repository,
                                 LiveMapWebSocketHandler webSocketHandler) {
        this.repository = repository;
        this.webSocketHandler = webSocketHandler;
    }

    public record GpsUpdate(double lat, double lng, Integer etaMinutes) {}

    public record LiveExecutionDTO(UUID id, UUID vehicleId, String driverName, String status,
                                    Double currentLat, Double currentLng, int currentStop,
                                    int totalStops, Double etaMinutes, Double deviationKm,
                                    Double plannedDistanceKm, Double actualDistanceKm,
                                    Instant startedAt) {}

    public UUID start(UUID organizationId, UUID vehicleId, UUID routeId,
                      String driverName, int totalStops, Double plannedDistanceKm) {
        LiveExecutionJpaEntity entity = new LiveExecutionJpaEntity();
        entity.setOrganizationId(organizationId);
        entity.setVehicleId(vehicleId);
        entity.setRouteId(routeId);
        entity.setDriverName(driverName);
        entity.setStatus("ACTIVE");
        entity.setCurrentStop(0);
        entity.setTotalStops(totalStops);
        entity.setActualDistanceKm(BigDecimal.ZERO);
        if (plannedDistanceKm != null) entity.setPlannedDistanceKm(BigDecimal.valueOf(plannedDistanceKm));
        entity.setStartedAt(Instant.now());
        entity.setCreatedAt(Instant.now());
        LiveExecutionJpaEntity saved = repository.save(entity);
        return saved.getId();
    }

    public void updateGps(UUID organizationId, UUID executionId, GpsUpdate update) {
        repository.findById(executionId).ifPresent(entity -> {
            if (!entity.getOrganizationId().equals(organizationId)) return;
            entity.setCurrentLat(BigDecimal.valueOf(update.lat()));
            entity.setCurrentLng(BigDecimal.valueOf(update.lng()));
            if (update.etaMinutes() != null) entity.setEtaMinutes(update.etaMinutes());
            repository.save(entity);
            webSocketHandler.broadcastUpdate(organizationId, toDTO(entity));
        });
    }

    public void completeStop(UUID organizationId, UUID executionId) {
        repository.findById(executionId).ifPresent(entity -> {
            if (!entity.getOrganizationId().equals(organizationId)) return;
            entity.setCurrentStop(entity.getCurrentStop() + 1);
            if (entity.getCurrentStop() >= entity.getTotalStops()) {
                entity.setStatus("COMPLETED");
                entity.setCompletedAt(Instant.now());
            }
            repository.save(entity);
            webSocketHandler.broadcastUpdate(organizationId, toDTO(entity));
        });
    }

    @Transactional(readOnly = true)
    public List<LiveExecutionDTO> getActive(UUID organizationId) {
        return repository.findByOrganizationIdAndStatus(organizationId, "ACTIVE")
                .stream().map(this::toDTO).toList();
    }

    private LiveExecutionDTO toDTO(LiveExecutionJpaEntity e) {
        return new LiveExecutionDTO(
                e.getId(), e.getVehicleId(), e.getDriverName(), e.getStatus(),
                e.getCurrentLat() != null ? e.getCurrentLat().doubleValue() : null,
                e.getCurrentLng() != null ? e.getCurrentLng().doubleValue() : null,
                e.getCurrentStop(), e.getTotalStops(),
                e.getEtaMinutes() != null ? e.getEtaMinutes().doubleValue() : null,
                e.getDeviationKm() != null ? e.getDeviationKm().doubleValue() : null,
                e.getPlannedDistanceKm() != null ? e.getPlannedDistanceKm().doubleValue() : null,
                e.getActualDistanceKm() != null ? e.getActualDistanceKm().doubleValue() : null,
                e.getStartedAt()
        );
    }
}
