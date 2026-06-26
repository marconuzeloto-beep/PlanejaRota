package com.logicore.core.infrastructure.persistence.repository;

import com.logicore.core.domain.model.Customer;
import com.logicore.core.domain.repository.CustomerRepository;
import com.logicore.core.infrastructure.persistence.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final CustomerJpaRepository jpaRepository;
    private final CustomerMapper mapper;

    @Override
    public Customer save(Customer customer) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(customer)));
    }

    @Override
    public Optional<Customer> findById(UUID orgId, UUID customerId) {
        return jpaRepository.findByOrganizationIdAndId(orgId, customerId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Customer> findAll(UUID orgId) {
        return jpaRepository.findByOrganizationId(orgId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(UUID orgId, UUID customerId) {
        return jpaRepository.existsByOrganizationIdAndId(orgId, customerId);
    }
}
