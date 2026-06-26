package com.logicore.planning.infrastructure.persistence.repository;

import com.logicore.planning.domain.model.Route;
import com.logicore.planning.domain.model.RouteStatus;
import com.logicore.planning.domain.repository.RouteRepository;
import com.logicore.planning.infrastructure.persistence.entity.RouteJpaEntity;
import com.logicore.planning.infrastructure.persistence.mapper.RouteMapper;
import com.logicore.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RouteRepositoryAdapter implements RouteRepository {

    private final RouteJpaRepository jpaRepository;
    private final RouteMapper mapper;

    public RouteRepositoryAdapter(RouteJpaRepository jpaRepository, RouteMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Route save(Route route) {
        RouteJpaEntity entity = mapper.toEntity(route);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Route> findById(UUID organizationId, UUID routeId) {
        return jpaRepository.findByIdAndOrganizationId(routeId, organizationId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Route> findByOrganizationId(UUID organizationId) {
        return jpaRepository.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public List<Route> findByOrganizationIdAndDate(UUID organizationId, LocalDate date) {
        return jpaRepository.findByOrganizationIdAndPlannedDate(organizationId, date).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public List<Route> findByOrganizationIdAndStatus(UUID organizationId, RouteStatus status) {
        return jpaRepository.findByOrganizationIdAndStatus(organizationId, status.name()).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsById(UUID organizationId, UUID routeId) {
        return jpaRepository.existsByIdAndOrganizationId(routeId, organizationId);
    }
}
