package com.planejarota.domain.model.fleet;

import com.planejarota.domain.valueobject.Weight;

import java.util.UUID;

public class Vehicle {

    public enum Status { AVAILABLE, IN_USE, MAINTENANCE, INACTIVE }
    public enum Type { MOTORCYCLE, CAR, VAN, TRUCK_SMALL, TRUCK_LARGE }

    private UUID id;
    private String licensePlate;
    private String model;
    private Type type;
    private Weight capacity;
    private Status status;
    private String notes;

    private Vehicle() {}

    public static Vehicle create(String licensePlate, String model, Type type, Weight capacity) {
        if (licensePlate == null || licensePlate.isBlank()) throw new IllegalArgumentException("Placa é obrigatória");
        if (model == null || model.isBlank()) throw new IllegalArgumentException("Modelo é obrigatório");
        if (type == null) throw new IllegalArgumentException("Tipo é obrigatório");
        if (capacity == null) throw new IllegalArgumentException("Capacidade é obrigatória");

        Vehicle v = new Vehicle();
        v.id = UUID.randomUUID();
        v.licensePlate = licensePlate.toUpperCase().trim();
        v.model = model;
        v.type = type;
        v.capacity = capacity;
        v.status = Status.AVAILABLE;
        return v;
    }

    public static Vehicle reconstitute(UUID id, String licensePlate, String model,
                                        Type type, Weight capacity, Status status, String notes) {
        Vehicle v = new Vehicle();
        v.id = id;
        v.licensePlate = licensePlate;
        v.model = model;
        v.type = type;
        v.capacity = capacity;
        v.status = status;
        v.notes = notes;
        return v;
    }

    public boolean isAvailable() {
        return status == Status.AVAILABLE;
    }

    public void markInUse() {
        if (!isAvailable()) throw new IllegalStateException("Veículo não está disponível: " + licensePlate);
        this.status = Status.IN_USE;
    }

    public void markAvailable() {
        this.status = Status.AVAILABLE;
    }

    public void sendToMaintenance(String reason) {
        this.status = Status.MAINTENANCE;
        this.notes = reason;
    }

    public UUID getId() { return id; }
    public String getLicensePlate() { return licensePlate; }
    public String getModel() { return model; }
    public Type getType() { return type; }
    public Weight getCapacity() { return capacity; }
    public Status getStatus() { return status; }
    public String getNotes() { return notes; }
}
