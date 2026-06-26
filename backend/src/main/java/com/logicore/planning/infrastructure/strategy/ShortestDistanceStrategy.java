package com.logicore.planning.infrastructure.strategy;

import com.logicore.core.domain.model.Vehicle;
import com.logicore.planning.domain.strategy.OptimizationStrategy;
import com.logicore.planning.domain.strategy.StrategyConfig;
import com.logicore.planning.domain.strategy.StrategyType;
import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.DecisionStep;
import com.logicore.planning.domain.valueobject.PlanningOrder;
import com.logicore.planning.domain.valueobject.RouteConstraints;
import com.logicore.planning.domain.valueobject.RouteMetrics;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Nearest Neighbor heuristic — sempre escolhe o pedido não visitado mais próximo da posição atual.
 */
@Component
public class ShortestDistanceStrategy implements OptimizationStrategy {

    private static final String IDENTIFIER  = "SHORTEST_DISTANCE_V1";
    private static final String DESCRIPTION = "Vizinho mais próximo — minimiza distância total com greedy Nearest Neighbor";

    @Override public StrategyType getType()       { return StrategyType.SHORTEST_DISTANCE; }
    @Override public String getIdentifier()       { return IDENTIFIER; }
    @Override public String getDescription()      { return DESCRIPTION; }

    @Override
    public DecisionResult execute(List<PlanningOrder> orders, Vehicle vehicle,
                                   GeoCoordinate depot, RouteConstraints constraints,
                                   StrategyConfig config) {

        List<PlanningOrder> remaining = new ArrayList<>(orders);
        List<DecisionStep> steps = new ArrayList<>(orders.size());

        GeoCoordinate current = depot;
        double cumDistance = 0.0;
        int cumTimeMinutes = 0;
        double totalWeight = 0.0;
        int violations = 0;
        LocalTime departure = LocalTime.of(8, 0);

        for (int position = 1; !remaining.isEmpty(); position++) {
            PlanningOrder nearest = findNearest(current, remaining);
            remaining.remove(nearest);

            double dist = current.distanceInKmTo(nearest.deliveryLocation());
            cumDistance += dist;
            int travelMin = (int) Math.ceil(dist / constraints.averageSpeedKmh() * 60.0);
            cumTimeMinutes += travelMin + constraints.stopDurationMinutes();

            LocalTime arrival = departure.plusMinutes(cumTimeMinutes - constraints.stopDurationMinutes());
            boolean inWindow = nearest.timeWindow() == null || nearest.timeWindow().contains(arrival);
            String warning = inWindow ? null : buildWarning(nearest, arrival);
            if (!inWindow) violations++;

            totalWeight += nearest.weight().kilograms();

            steps.add(DecisionStep.nearestNeighbor(
                    position, nearest.orderId(), nearest.customerId(), nearest.customerName(),
                    nearest.deliveryLocation(), dist, cumDistance, arrival, inWindow, warning));

            current = nearest.deliveryLocation();
        }

        RouteMetrics metrics = buildMetrics(vehicle, cumDistance, cumTimeMinutes,
                totalWeight, violations);
        return DecisionResult.of(StrategyType.SHORTEST_DISTANCE, config, steps, metrics);
    }

    private PlanningOrder findNearest(GeoCoordinate from, List<PlanningOrder> candidates) {
        PlanningOrder best = null;
        double bestDist = Double.MAX_VALUE;
        for (PlanningOrder o : candidates) {
            double d = from.distanceInKmTo(o.deliveryLocation());
            if (d < bestDist) { bestDist = d; best = o; }
        }
        return best;
    }

    private String buildWarning(PlanningOrder o, LocalTime arrival) {
        return String.format("Chegada prevista %s fora da janela %s–%s",
                arrival, o.timeWindow().openTime(), o.timeWindow().closeTime());
    }

    private RouteMetrics buildMetrics(Vehicle vehicle, double dist, int minutes,
                                       double weight, int violations) {
        double cap = vehicle.getCapacity().kilograms();
        double capPct = cap > 0 ? (weight / cap) * 100.0 : 0.0;
        BigDecimal cost = vehicle.getCostPerKm() == null ? BigDecimal.ZERO
                : vehicle.getCostPerKm().amount().multiply(BigDecimal.valueOf(dist))
                         .setScale(2, RoundingMode.HALF_UP);
        return new RouteMetrics(dist, minutes, cost, weight, capPct, violations, violations == 0);
    }
}
