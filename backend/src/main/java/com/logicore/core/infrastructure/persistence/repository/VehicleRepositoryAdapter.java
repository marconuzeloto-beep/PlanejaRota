package com.logicore.core.infrastructure.persistence.repository;

import com.logicore.core.domain.model.Vehicle;
import com.logicore.core.domain.model.VehicleStatus;
import com.logicore.core.domain.repository.VehicleRepository;
import com.logicore.core.infrastructure.persistence.mapper.VehicleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class VehicleRepositoryAdapter implements VehicleRepository {

    private final VehicleJpaRepository jpaRepository;
    private final VehicleMapper mapper;

    @Override
    public Vehicle save(Vehicle vehicle) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(vehicle)));
    }

    @Override
    public Optional<Vehicle> findById(UUID orgId, UUID vehicleId) {
        return jpaRepository.findByOrganizationIdAndId(orgId, vehicleId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Vehicle> findAvailable(UUID orgId) {
        return jpaRepository.findByOrganizationIdAndStatus(orgId, VehicleStatus.AVAILABLE.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Vehicle> findAll(UUID orgId) {
        return jpaRepository.findByOrganizationId(orgId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
