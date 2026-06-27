package com.logicore.benchmark.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "benchmark_entries")
public class BenchmarkEntryJpaEntity {

    @Id UUID id;
    @Column(name = "organization_id", nullable = false) UUID organizationId;
    @Column(name = "strategy_identifier", nullable = false) String strategyIdentifier;
    @Column(name = "strategy_type", nullable = false) String strategyType;
    @Column(name = "order_count", nullable = false) int orderCount;
    @Column(name = "execution_time_ms", nullable = false) long executionTimeMs;
    @Column(name = "total_distance_km", nullable = false) double totalDistanceKm;
    @Column(name = "total_time_minutes", nullable = false) int totalTimeMinutes;
    @Column(name = "capacity_usage_pct") double capacityUsagePct;
    @Column(name = "stops_with_violations") int stopsWithViolations;
    @Column(name = "feasible") boolean feasible;
    @Column(name = "route_score") Double routeScore;
    @Column(name = "score_grade") String scoreGrade;
    @Column(name = "two_opt_applied") boolean twoOptApplied;
    @Column(name = "two_opt_improvement_km") double twoOptImprovementKm;
    @Column(name = "memory_bytes") Long memoryBytes;
    @Column(name = "notes") String notes;
    @Column(name = "created_at") Instant createdAt;

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    public String getStrategyIdentifier() { return strategyIdentifier; }
    public void setStrategyIdentifier(String strategyIdentifier) { this.strategyIdentifier = strategyIdentifier; }
    public String getStrategyType() { return strategyType; }
    public void setStrategyType(String strategyType) { this.strategyType = strategyType; }
    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }
    public int getTotalTimeMinutes() { return totalTimeMinutes; }
    public void setTotalTimeMinutes(int totalTimeMinutes) { this.totalTimeMinutes = totalTimeMinutes; }
    public double getCapacityUsagePct() { return capacityUsagePct; }
    public void setCapacityUsagePct(double capacityUsagePct) { this.capacityUsagePct = capacityUsagePct; }
    public int getStopsWithViolations() { return stopsWithViolations; }
    public void setStopsWithViolations(int stopsWithViolations) { this.stopsWithViolations = stopsWithViolations; }
    public boolean isFeasible() { return feasible; }
    public void setFeasible(boolean feasible) { this.feasible = feasible; }
    public Double getRouteScore() { return routeScore; }
    public void setRouteScore(Double routeScore) { this.routeScore = routeScore; }
    public String getScoreGrade() { return scoreGrade; }
    public void setScoreGrade(String scoreGrade) { this.scoreGrade = scoreGrade; }
    public boolean isTwoOptApplied() { return twoOptApplied; }
    public void setTwoOptApplied(boolean twoOptApplied) { this.twoOptApplied = twoOptApplied; }
    public double getTwoOptImprovementKm() { return twoOptImprovementKm; }
    public void setTwoOptImprovementKm(double twoOptImprovementKm) { this.twoOptImprovementKm = twoOptImprovementKm; }
    public Long getMemoryBytes() { return memoryBytes; }
    public void setMemoryBytes(Long memoryBytes) { this.memoryBytes = memoryBytes; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
