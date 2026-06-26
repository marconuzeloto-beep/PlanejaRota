package com.logicore.planning.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "routes")
public class RouteJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "organization_id", nullable = false, columnDefinition = "uuid")
    private UUID organizationId;

    @Column(name = "vehicle_id", nullable = false, columnDefinition = "uuid")
    private UUID vehicleId;

    @Column(name = "depot_lat", nullable = false)
    private Double depotLat;

    @Column(name = "depot_lng", nullable = false)
    private Double depotLng;

    @Column(name = "planned_date", nullable = false)
    private LocalDate plannedDate;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "max_weight_kg", nullable = false)
    private Double maxWeightKg;

    @Column(name = "avg_speed_kmh", nullable = false)
    private Double avgSpeedKmh;

    @Column(name = "stop_duration_minutes", nullable = false)
    private Integer stopDurationMinutes;

    @Column(name = "hard_time_windows", nullable = false)
    private Boolean hardTimeWindows;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "decision_result", columnDefinition = "jsonb")
    private String decisionResultJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sequence_number ASC")
    private List<RouteStopJpaEntity> stops = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    public UUID getVehicleId() { return vehicleId; }
    public void setVehicleId(UUID vehicleId) { this.vehicleId = vehicleId; }
    public Double getDepotLat() { return depotLat; }
    public void setDepotLat(Double depotLat) { this.depotLat = depotLat; }
    public Double getDepotLng() { return depotLng; }
    public void setDepotLng(Double depotLng) { this.depotLng = depotLng; }
    public LocalDate getPlannedDate() { return plannedDate; }
    public void setPlannedDate(LocalDate plannedDate) { this.plannedDate = plannedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getMaxWeightKg() { return maxWeightKg; }
    public void setMaxWeightKg(Double maxWeightKg) { this.maxWeightKg = maxWeightKg; }
    public Double getAvgSpeedKmh() { return avgSpeedKmh; }
    public void setAvgSpeedKmh(Double avgSpeedKmh) { this.avgSpeedKmh = avgSpeedKmh; }
    public Integer getStopDurationMinutes() { return stopDurationMinutes; }
    public void setStopDurationMinutes(Integer stopDurationMinutes) { this.stopDurationMinutes = stopDurationMinutes; }
    public Boolean getHardTimeWindows() { return hardTimeWindows; }
    public void setHardTimeWindows(Boolean hardTimeWindows) { this.hardTimeWindows = hardTimeWindows; }
    public String getDecisionResultJson() { return decisionResultJson; }
    public void setDecisionResultJson(String decisionResultJson) { this.decisionResultJson = decisionResultJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public List<RouteStopJpaEntity> getStops() { return stops; }
    public void setStops(List<RouteStopJpaEntity> stops) { this.stops = stops; }
}
