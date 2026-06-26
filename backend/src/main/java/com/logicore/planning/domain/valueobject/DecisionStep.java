package com.logicore.planning.domain.valueobject;

import com.logicore.shared.domain.valueobject.GeoCoordinate;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Representa UMA decisão do algoritmo — por que esta parada foi colocada nesta posição.
 * É a unidade de explicabilidade do sistema.
 * Imutável — criado pelo algoritmo, nunca modificado após.
 */
public record DecisionStep(
        int position,
        UUID orderId,
        UUID customerId,
        String customerName,
        GeoCoordinate location,
        DecisionReason reason,
        double distanceFromPreviousKm,
        double cumulativeDistanceKm,
        LocalTime estimatedArrivalTime,
        boolean withinTimeWindow,
        String timeWindowWarning,
        double priorityScore
) {
    /**
     * Factory para parada que foi escolhida pelo critério de vizinho mais próximo.
     */
    public static DecisionStep nearestNeighbor(
            int position, UUID orderId, UUID customerId, String customerName,
            GeoCoordinate location, double distFromPrev, double cumDist,
            LocalTime estimatedArrival, boolean withinWindow, String windowWarning
    ) {
        return new DecisionStep(
                position, orderId, customerId, customerName, location,
                DecisionReason.nearestNeighbor(distFromPrev),
                distFromPrev, cumDist, estimatedArrival, withinWindow, windowWarning, 0
        );
    }

    /**
     * Factory para parada escolhida por prioridade alta.
     */
    public static DecisionStep priorityFirst(
            int position, UUID orderId, UUID customerId, String customerName,
            GeoCoordinate location, int priority, double distFromPrev, double cumDist,
            LocalTime estimatedArrival, boolean withinWindow, String windowWarning
    ) {
        return new DecisionStep(
                position, orderId, customerId, customerName, location,
                DecisionReason.priorityOverride(priority, distFromPrev),
                distFromPrev, cumDist, estimatedArrival, withinWindow, windowWarning, priority
        );
    }

    /**
     * Factory para parada escolhida por score híbrido.
     */
    public static DecisionStep hybrid(
            int position, UUID orderId, UUID customerId, String customerName,
            GeoCoordinate location, double score, double distWeight, double prioWeight,
            double distFromPrev, double cumDist,
            LocalTime estimatedArrival, boolean withinWindow, String windowWarning
    ) {
        return new DecisionStep(
                position, orderId, customerId, customerName, location,
                DecisionReason.hybridScore(score, distWeight, prioWeight),
                distFromPrev, cumDist, estimatedArrival, withinWindow, windowWarning, score
        );
    }
}
