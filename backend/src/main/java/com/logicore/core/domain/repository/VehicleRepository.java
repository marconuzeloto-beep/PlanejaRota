package com.logicore.core.domain.repository;

import com.logicore.core.domain.model.Vehicle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository {
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findById(UUID orgId, UUID vehicleId);
    List<Vehicle> findAvailable(UUID orgId);
    List<Vehicle> findAll(UUID orgId);
}
