package com.logicore.analytics.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analytics_daily_snapshots",
       uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "snapshot_date"}))
public class DailySnapshotJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "total_routes", nullable = false)
    private int totalRoutes;

    @Column(name = "completed_routes", nullable = false)
    private int completedRoutes;

    @Column(name = "total_distance_km", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDistanceKm;

    @Column(name = "total_duration_min", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDurationMin;

    @Column(name = "avg_route_score", precision = 5, scale = 4)
    private BigDecimal avgRouteScore;

    @Column(name = "total_orders_planned", nullable = false)
    private int totalOrdersPlanned;

    @Column(name = "total_orders_delivered", nullable = false)
    private int totalOrdersDelivered;

    @Column(name = "fuel_consumption_l", precision = 10, scale = 2)
    private BigDecimal fuelConsumptionL;

    @Column(name = "co2_kg", precision = 10, scale = 2)
    private BigDecimal co2Kg;

    @Column(name = "on_time_deliveries", nullable = false)
    private int onTimeDeliveries;

    @Column(name = "constraint_violations", nullable = false)
    private int constraintViolations;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    public LocalDate getSnapshotDate() { return snapshotDate; }
    public void setSnapshotDate(LocalDate snapshotDate) { this.snapshotDate = snapshotDate; }
    public int getTotalRoutes() { return totalRoutes; }
    public void setTotalRoutes(int totalRoutes) { this.totalRoutes = totalRoutes; }
    public int getCompletedRoutes() { return completedRoutes; }
    public void setCompletedRoutes(int completedRoutes) { this.completedRoutes = completedRoutes; }
    public BigDecimal getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(BigDecimal totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }
    public BigDecimal getTotalDurationMin() { return totalDurationMin; }
    public void setTotalDurationMin(BigDecimal totalDurationMin) { this.totalDurationMin = totalDurationMin; }
    public BigDecimal getAvgRouteScore() { return avgRouteScore; }
    public void setAvgRouteScore(BigDecimal avgRouteScore) { this.avgRouteScore = avgRouteScore; }
    public int getTotalOrdersPlanned() { return totalOrdersPlanned; }
    public void setTotalOrdersPlanned(int totalOrdersPlanned) { this.totalOrdersPlanned = totalOrdersPlanned; }
    public int getTotalOrdersDelivered() { return totalOrdersDelivered; }
    public void setTotalOrdersDelivered(int totalOrdersDelivered) { this.totalOrdersDelivered = totalOrdersDelivered; }
    public BigDecimal getFuelConsumptionL() { return fuelConsumptionL; }
    public void setFuelConsumptionL(BigDecimal fuelConsumptionL) { this.fuelConsumptionL = fuelConsumptionL; }
    public BigDecimal getCo2Kg() { return co2Kg; }
    public void setCo2Kg(BigDecimal co2Kg) { this.co2Kg = co2Kg; }
    public int getOnTimeDeliveries() { return onTimeDeliveries; }
    public void setOnTimeDeliveries(int onTimeDeliveries) { this.onTimeDeliveries = onTimeDeliveries; }
    public int getConstraintViolations() { return constraintViolations; }
    public void setConstraintViolations(int constraintViolations) { this.constraintViolations = constraintViolations; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
