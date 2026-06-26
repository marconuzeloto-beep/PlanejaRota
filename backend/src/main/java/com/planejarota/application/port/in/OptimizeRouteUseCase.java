package com.planejarota.application.port.in;

import java.util.List;
import java.util.UUID;

public interface OptimizeRouteUseCase {

    record Result(UUID routeId, List<UUID> optimizedStopOrder, double totalDistanceKm) {}

    Result execute(UUID routeId);
}
