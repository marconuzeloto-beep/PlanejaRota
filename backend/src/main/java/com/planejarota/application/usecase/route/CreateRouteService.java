package com.planejarota.application.usecase.route;

import com.planejarota.application.port.in.CreateRouteUseCase;
import com.planejarota.domain.exception.RouteNotFoundException;
import com.planejarota.domain.model.fleet.Vehicle;
import com.planejarota.domain.model.route.Route;
import com.planejarota.domain.repository.RouteRepository;
import com.planejarota.domain.repository.VehicleRepository;
import com.planejarota.domain.valueobject.GeoCoordinate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateRouteService implements CreateRouteUseCase {

    private final RouteRepository routeRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public UUID execute(Command command) {
        Vehicle vehicle = vehicleRepository.findById(command.vehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Veículo não encontrado: " + command.vehicleId()));

        if (!vehicle.isAvailable()) {
            throw new IllegalStateException("Veículo não está disponível para atribuição: " + vehicle.getLicensePlate());
        }

        GeoCoordinate depot = new GeoCoordinate(command.depotLatitude(), command.depotLongitude());

        Route route = Route.create(
                command.name(),
                vehicle,
                depot,
                command.scheduledDate(),
                command.departureTime()
        );

        Route saved = routeRepository.save(route);
        return saved.getId();
    }
}
