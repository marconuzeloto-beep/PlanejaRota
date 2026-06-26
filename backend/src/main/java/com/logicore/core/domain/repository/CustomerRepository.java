package com.logicore.core.domain.repository;

import com.logicore.core.domain.model.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(UUID orgId, UUID customerId);
    List<Customer> findAll(UUID orgId);
    boolean existsById(UUID orgId, UUID customerId);
}
