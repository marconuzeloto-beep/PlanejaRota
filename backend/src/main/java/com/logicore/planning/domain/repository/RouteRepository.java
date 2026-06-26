package com.logicore.planning.domain.repository;

import com.logicore.planning.domain.model.Route;
import com.logicore.planning.domain.model.RouteStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RouteRepository {
    Route save(Route route);
    Optional<Route> findById(UUID organizationId, UUID routeId);
    List<Route> findByOrganizationId(UUID organizationId);
    List<Route> findByOrganizationIdAndDate(UUID organizationId, LocalDate date);
    List<Route> findByOrganizationIdAndStatus(UUID organizationId, RouteStatus status);
    boolean existsById(UUID organizationId, UUID routeId);
}
