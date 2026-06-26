package com.planejarota.domain.repository;

import com.planejarota.domain.model.fleet.Vehicle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository {
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findById(UUID id);
    List<Vehicle> findAvailable();
    List<Vehicle> findAll();
    void delete(UUID id);
}
