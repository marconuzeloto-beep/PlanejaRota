package com.planejarota.application.usecase.route;

import com.planejarota.application.port.in.OptimizeRouteUseCase;
import com.planejarota.domain.exception.RouteNotFoundException;
import com.planejarota.domain.model.route.Route;
import com.planejarota.domain.repository.RouteRepository;
import com.planejarota.domain.service.RouteOptimizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OptimizeRouteService implements OptimizeRouteUseCase {

    private final RouteRepository routeRepository;
    private final RouteOptimizationService optimizationService;

    @Override
    @Transactional
    public Result execute(UUID routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId));

        List<UUID> optimizedOrder = optimizationService.optimize(route);
        route.reorderStops(optimizedOrder);

        double distance = route.calculateTotalDistance();
        routeRepository.save(route);

        return new Result(routeId, optimizedOrder, distance);
    }
}
