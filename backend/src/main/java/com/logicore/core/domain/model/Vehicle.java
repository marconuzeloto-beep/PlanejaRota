package com.logicore.core.domain.model;

import com.logicore.shared.domain.exception.BusinessRuleException;
import com.logicore.shared.domain.valueobject.Money;
import com.logicore.shared.domain.valueobject.Weight;

import java.time.Instant;
import java.util.UUID;

public class Vehicle {

    private final UUID id;
    private final UUID organizationId;
    private final String licensePlate;
    private final String model;
    private final VehicleType type;
    private final Weight capacity;
    private final Money costPerKm;
    private VehicleStatus status;
    private String notes;
    private final Instant createdAt;

    private Vehicle(UUID id, UUID organizationId, String licensePlate, String model,
                    VehicleType type, Weight capacity, Money costPerKm,
                    VehicleStatus status, String notes, Instant createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.licensePlate = licensePlate;
        this.model = model;
        this.type = type;
        this.capacity = capacity;
        this.costPerKm = costPerKm;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public static Vehicle create(UUID orgId, String licensePlate, String model,
                                 VehicleType type, Weight capacity, Money costPerKm) {
        if (orgId == null) throw new IllegalArgumentException("Organization ID é obrigatório");
        if (licensePlate == null || licensePlate.isBlank()) throw new IllegalArgumentException("Placa é obrigatória");
        if (model == null || model.isBlank()) throw new IllegalArgumentException("Modelo é obrigatório");
        if (type == null) throw new IllegalArgumentException("Tipo do veículo é obrigatório");
        if (capacity == null) throw new IllegalArgumentException("Capacidade é obrigatória");

        return new Vehicle(UUID.randomUUID(), orgId, licensePlate, model,
                type, capacity, costPerKm, VehicleStatus.AVAILABLE, null, Instant.now());
    }

    public static Vehicle reconstitute(UUID id, UUID organizationId, String licensePlate, String model,
                                       VehicleType type, Weight capacity, Money costPerKm,
                                       VehicleStatus status, String notes, Instant createdAt) {
        return new Vehicle(id, organizationId, licensePlate, model,
                type, capacity, costPerKm, status, notes, createdAt);
    }

    public void markInUse() {
        if (this.status != VehicleStatus.AVAILABLE) {
            throw new BusinessRuleException(
                    "Veículo não está disponível para uso. Status atual: " + this.status);
        }
        this.status = VehicleStatus.IN_USE;
    }

    public void markAvailable() {
        this.status = VehicleStatus.AVAILABLE;
        this.notes = null;
    }

    public void sendToMaintenance(String reason) {
        this.status = VehicleStatus.MAINTENANCE;
        this.notes = reason;
    }

    public void inactivate() {
        this.status = VehicleStatus.INACTIVE;
    }

    public boolean isAvailable() {
        return this.status == VehicleStatus.AVAILABLE;
    }

    public Money calculateCostForDistance(double km) {
        if (costPerKm == null) return Money.zero();
        return costPerKm.multiply(km);
    }

    public UUID getId() { return id; }
    public UUID getOrganizationId() { return organizationId; }
    public String getLicensePlate() { return licensePlate; }
    public String getModel() { return model; }
    public VehicleType getType() { return type; }
    public Weight getCapacity() { return capacity; }
    public Money getCostPerKm() { return costPerKm; }
    public VehicleStatus getStatus() { return status; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
}
