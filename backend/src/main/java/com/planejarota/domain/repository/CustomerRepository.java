package com.planejarota.domain.repository;

import com.planejarota.domain.model.route.Customer;
import com.planejarota.domain.valueobject.GeoCoordinate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(UUID id);
    List<Customer> findAll();
    List<Customer> findNearby(GeoCoordinate center, double radiusKm);
    void delete(UUID id);
}
