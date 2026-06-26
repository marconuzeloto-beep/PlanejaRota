package com.logicore.api.dto.response;

import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.DecisionStep;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MapRouteDTO(
        UUID routeId,
        String strategyIdentifier,
        String strategyType,
        double depotLat,
        double depotLng,
        List<MapStopDTO> stops,
        double totalDistanceKm,
        int totalEstimatedTimeMinutes,
        BigDecimal estimatedCostBrl,
        double capacityUsagePercent,
        int stopsWithTimeViolation,
        boolean feasible
) {
    public static MapRouteDTO from(UUID routeId, double depotLat, double depotLng, DecisionResult result) {
        List<MapStopDTO> stops = result.orderedSteps().stream()
                .map(MapRouteDTO::toStopDTO)
                .toList();

        return new MapRouteDTO(
                routeId,
                result.strategyConfig() != null ? result.strategyType().name() : result.strategyType().name(),
                result.strategyType().name(),
                depotLat, depotLng, stops,
                result.metrics().totalDistanceKm(),
                result.metrics().totalEstimatedTimeMinutes(),
                result.metrics().estimatedCostBRL(),
                result.metrics().capacityUsagePercent(),
                result.metrics().stopsWithTimeViolation(),
                result.metrics().feasible()
        );
    }

    private static MapStopDTO toStopDTO(DecisionStep step) {
        return new MapStopDTO(
                step.position(),
                step.orderId(),
                step.customerId(),
                step.customerName(),
                step.location().latitude(),
                step.location().longitude(),
                step.distanceFromPreviousKm(),
                step.cumulativeDistanceKm(),
                step.estimatedArrivalTime(),
                step.withinTimeWindow(),
                step.timeWindowWarning(),
                step.reason().type().name(),
                step.reason().detail()
        );
    }
}
