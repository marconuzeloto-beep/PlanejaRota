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
import java.util.Comparator;
import java.util.List;

/**
 * Prioridade decrescente, desempate por distância do depot.
 */
@Component
public class PriorityFirstStrategy implements OptimizationStrategy {

    private static final String IDENTIFIER  = "PRIORITY_FIRST_V1";
    private static final String DESCRIPTION = "Prioridade decrescente — pedidos críticos visitados primeiro, desempate por distância";

    @Override public StrategyType getType()  { return StrategyType.PRIORITY_FIRST; }
    @Override public String getIdentifier()  { return IDENTIFIER; }
    @Override public String getDescription() { return DESCRIPTION; }

    @Override
    public DecisionResult execute(List<PlanningOrder> orders, Vehicle vehicle,
                                   GeoCoordinate depot, RouteConstraints constraints,
                                   StrategyConfig config) {

        List<PlanningOrder> sorted = new ArrayList<>(orders);
        sorted.sort(Comparator
                .comparingInt((PlanningOrder o) -> o.priority().value()).reversed()
                .thenComparingDouble(o -> depot.distanceInKmTo(o.deliveryLocation())));

        List<DecisionStep> steps = new ArrayList<>(sorted.size());
        GeoCoordinate current = depot;
        double cumDistance = 0.0;
        int cumTimeMinutes = 0;
        double totalWeight = 0.0;
        int violations = 0;
        LocalTime departure = LocalTime.of(8, 0);

        for (int i = 0; i < sorted.size(); i++) {
            PlanningOrder o = sorted.get(i);
            double dist = current.distanceInKmTo(o.deliveryLocation());
            cumDistance += dist;
            int travelMin = (int) Math.ceil(dist / constraints.averageSpeedKmh() * 60.0);
            cumTimeMinutes += travelMin + constraints.stopDurationMinutes();

            LocalTime arrival = departure.plusMinutes(cumTimeMinutes - constraints.stopDurationMinutes());
            boolean inWindow = o.timeWindow() == null || o.timeWindow().contains(arrival);
            String warning = inWindow ? null : buildWarning(o, arrival);
            if (!inWindow) violations++;

            totalWeight += o.weight().kilograms();

            steps.add(DecisionStep.priorityFirst(
                    i + 1, o.orderId(), o.customerId(), o.customerName(),
                    o.deliveryLocation(), o.priority().value(),
                    dist, cumDistance, arrival, inWindow, warning));

            current = o.deliveryLocation();
        }

        RouteMetrics metrics = buildMetrics(vehicle, cumDistance, cumTimeMinutes, totalWeight, violations);
        return DecisionResult.of(StrategyType.PRIORITY_FIRST, config, steps, metrics);
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
