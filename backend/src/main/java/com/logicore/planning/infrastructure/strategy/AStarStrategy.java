package com.logicore.planning.infrastructure.strategy;

import com.logicore.core.domain.model.Vehicle;
import com.logicore.planning.domain.strategy.OptimizationStrategy;
import com.logicore.planning.domain.strategy.StrategyConfig;
import com.logicore.planning.domain.strategy.StrategyType;
import com.logicore.planning.domain.valueobject.DecisionReason;
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
 * A* Heurística para construção de rota.
 *
 * Adaptação do A* para TSP construtivo:
 *   f(candidate) = g(candidate) + h(candidate)
 *   g = distância acumulada até o candidato
 *   h = distância do candidato ao cliente não visitado mais próximo (lower bound)
 *
 * Diferencia-se do Nearest Neighbor porque a heurística h "olha para frente",
 * evitando caminhos que se isolam em clusters distantes.
 *
 * Complexidade: O(n²) — mesma que NN mas com melhor qualidade em instâncias clustered.
 */
@Component
public class AStarStrategy implements OptimizationStrategy {

    private static final String IDENTIFIER  = "A_STAR_V1";
    private static final String DESCRIPTION = "A* Heurístico — minimiza f=g+h olhando para frente";

    @Override public StrategyType getType()  { return StrategyType.A_STAR; }
    @Override public String getIdentifier()  { return IDENTIFIER; }
    @Override public String getDescription() { return DESCRIPTION; }

    @Override
    public DecisionResult execute(List<PlanningOrder> orders, Vehicle vehicle,
                                   GeoCoordinate depot, RouteConstraints constraints,
                                   StrategyConfig config) {
        List<PlanningOrder> remaining = new ArrayList<>(orders);
        List<DecisionStep> steps = new ArrayList<>(orders.size());
        GeoCoordinate current = depot;
        double cumDist = 0; int cumTime = 0; double totalWeight = 0; int violations = 0;
        LocalTime departure = LocalTime.of(8, 0);

        for (int position = 1; !remaining.isEmpty(); position++) {
            PlanningOrder chosen = chooseBestAstar(current, remaining);
            remaining.remove(chosen);

            double dist = current.distanceInKmTo(chosen.deliveryLocation());
            double heuristic = remaining.isEmpty() ? 0
                    : minDistToAny(chosen.deliveryLocation(), remaining);

            cumDist += dist;
            int travelMin = (int) Math.ceil(dist / constraints.averageSpeedKmh() * 60.0);
            cumTime += travelMin + constraints.stopDurationMinutes();
            LocalTime arrival = departure.plusMinutes(cumTime - constraints.stopDurationMinutes());
            boolean inWindow = chosen.timeWindow() == null || chosen.timeWindow().contains(arrival);
            if (!inWindow) violations++;
            totalWeight += chosen.weight().kilograms();

            steps.add(new DecisionStep(position, chosen.orderId(), chosen.customerId(),
                    chosen.customerName(), chosen.deliveryLocation(),
                    DecisionReason.aStarPath(heuristic, dist),
                    dist, cumDist, arrival, inWindow,
                    inWindow ? null : "Fora da janela de tempo", chosen.priority().value()));

            current = chosen.deliveryLocation();
        }

        RouteMetrics metrics = buildMetrics(vehicle, cumDist, cumTime, totalWeight, violations);
        return DecisionResult.of(StrategyType.A_STAR, config, steps, metrics);
    }

    private PlanningOrder chooseBestAstar(GeoCoordinate from, List<PlanningOrder> candidates) {
        PlanningOrder best = null;
        double bestF = Double.MAX_VALUE;
        for (PlanningOrder c : candidates) {
            double g = from.distanceInKmTo(c.deliveryLocation());
            double h = minDistToAny(c.deliveryLocation(), candidates.stream()
                    .filter(o -> o != c).toList());
            double f = g + 0.5 * h; // weight: 50% lookahead
            if (f < bestF) { bestF = f; best = c; }
        }
        return best;
    }

    private double minDistToAny(GeoCoordinate from, List<PlanningOrder> candidates) {
        return candidates.stream()
                .mapToDouble(c -> from.distanceInKmTo(c.deliveryLocation()))
                .min().orElse(0.0);
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
