package com.logicore.execution.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "live_executions")
public class LiveExecutionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "route_id")
    private UUID routeId;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(name = "driver_name", length = 128)
    private String driverName;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "current_lat", precision = 10, scale = 7)
    private BigDecimal currentLat;

    @Column(name = "current_lng", precision = 10, scale = 7)
    private BigDecimal currentLng;

    @Column(name = "current_stop", nullable = false)
    private int currentStop;

    @Column(name = "total_stops", nullable = false)
    private int totalStops;

    @Column(name = "planned_distance_km", precision = 10, scale = 2)
    private BigDecimal plannedDistanceKm;

    @Column(name = "actual_distance_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualDistanceKm;

    @Column(name = "eta_minutes")
    private Integer etaMinutes;

    @Column(name = "deviation_km", precision = 8, scale = 2)
    private BigDecimal deviationKm;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    public UUID getRouteId() { return routeId; }
    public void setRouteId(UUID routeId) { this.routeId = routeId; }
    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getCurrentLat() { return currentLat; }
    public void setCurrentLat(BigDecimal currentLat) { this.currentLat = currentLat; }
    public BigDecimal getCurrentLng() { return currentLng; }
    public void setCurrentLng(BigDecimal currentLng) { this.currentLng = currentLng; }
    public int getCurrentStop() { return currentStop; }
    public void setCurrentStop(int currentStop) { this.currentStop = currentStop; }
    public int getTotalStops() { return totalStops; }
    public void setTotalStops(int totalStops) { this.totalStops = totalStops; }
    public BigDecimal getPlannedDistanceKm() { return plannedDistanceKm; }
    public void setPlannedDistanceKm(BigDecimal plannedDistanceKm) { this.plannedDistanceKm = plannedDistanceKm; }
    public BigDecimal getActualDistanceKm() { return actualDistanceKm; }
    public void setActualDistanceKm(BigDecimal actualDistanceKm) { this.actualDistanceKm = actualDistanceKm; }
    public Integer getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(Integer etaMinutes) { this.etaMinutes = etaMinutes; }
    public BigDecimal getDeviationKm() { return deviationKm; }
    public void setDeviationKm(BigDecimal deviationKm) { this.deviationKm = deviationKm; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
