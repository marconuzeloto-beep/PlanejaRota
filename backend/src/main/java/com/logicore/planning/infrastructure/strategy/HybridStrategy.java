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
 * Score combinado: distanceWeight * proximidade + priorityWeight * (priority/5).
 * Pesos via StrategyConfig.weights("distance") e ("priority"). Defaults 60/40.
 */
@Component
public class HybridStrategy implements OptimizationStrategy {

    private static final String IDENTIFIER  = "HYBRID_V1";
    private static final String DESCRIPTION = "Score combinado de distância e prioridade com pesos configuráveis";

    @Override public StrategyType getType()  { return StrategyType.HYBRID; }
    @Override public String getIdentifier()  { return IDENTIFIER; }
    @Override public String getDescription() { return DESCRIPTION; }

    @Override
    public DecisionResult execute(List<PlanningOrder> orders, Vehicle vehicle,
                                   GeoCoordinate depot, RouteConstraints constraints,
                                   StrategyConfig config) {

        double distWeight = config.weight("distance", 0.6);
        double prioWeight = config.weight("priority", 0.4);

        List<PlanningOrder> remaining = new ArrayList<>(orders);
        List<DecisionStep> steps = new ArrayList<>(orders.size());

        GeoCoordinate current = depot;
        double cumDistance = 0.0;
        int cumTimeMinutes = 0;
        double totalWeight = 0.0;
        int violations = 0;
        LocalTime departure = LocalTime.of(8, 0);

        for (int position = 1; !remaining.isEmpty(); position++) {
            double maxDist = maxDistance(current, remaining);
            PlanningOrder best = selectBest(current, remaining, distWeight, prioWeight, maxDist);
            remaining.remove(best);

            double dist = current.distanceInKmTo(best.deliveryLocation());
            cumDistance += dist;
            int travelMin = (int) Math.ceil(dist / constraints.averageSpeedKmh() * 60.0);
            cumTimeMinutes += travelMin + constraints.stopDurationMinutes();

            LocalTime arrival = departure.plusMinutes(cumTimeMinutes - constraints.stopDurationMinutes());
            boolean inWindow = best.timeWindow() == null || best.timeWindow().contains(arrival);
            String warning = inWindow ? null : buildWarning(best, arrival);
            if (!inWindow) violations++;

            totalWeight += best.weight().kilograms();

            double score = score(current, best, distWeight, prioWeight, maxDist);
            steps.add(DecisionStep.hybrid(
                    position, best.orderId(), best.customerId(), best.customerName(),
                    best.deliveryLocation(), score, distWeight, prioWeight,
                    dist, cumDistance, arrival, inWindow, warning));

            current = best.deliveryLocation();
        }

        RouteMetrics metrics = buildMetrics(vehicle, cumDistance, cumTimeMinutes, totalWeight, violations);
        return DecisionResult.of(StrategyType.HYBRID, config, steps, metrics);
    }

    private PlanningOrder selectBest(GeoCoordinate from, List<PlanningOrder> candidates,
                                      double dw, double pw, double maxDist) {
        PlanningOrder best = null;
        double bestScore = -1.0;
        for (PlanningOrder o : candidates) {
            double s = score(from, o, dw, pw, maxDist);
            if (s > bestScore) { bestScore = s; best = o; }
        }
        return best;
    }

    private double score(GeoCoordinate from, PlanningOrder o, double dw, double pw, double maxDist) {
        double dist = from.distanceInKmTo(o.deliveryLocation());
        double prox = maxDist > 0 ? 1.0 - (dist / maxDist) : 1.0;
        return dw * prox + pw * (o.priority().value() / 5.0);
    }

    private double maxDistance(GeoCoordinate from, List<PlanningOrder> orders) {
        return orders.stream()
                .mapToDouble(o -> from.distanceInKmTo(o.deliveryLocation()))
                .max().orElse(1.0);
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
