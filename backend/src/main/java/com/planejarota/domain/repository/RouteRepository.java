package com.planejarota.domain.repository;

import com.planejarota.domain.model.route.Route;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saída para persistência de rotas.
 * O domínio define o contrato; a infraestrutura implementa.
 * Isso inverte a dependência: domínio → interface, infra → implementação.
 */
public interface RouteRepository {
    Route save(Route route);
    Optional<Route> findById(UUID id);
    List<Route> findByScheduledDate(LocalDate date);
    List<Route> findByVehicleId(UUID vehicleId);
    void delete(UUID id);
}
