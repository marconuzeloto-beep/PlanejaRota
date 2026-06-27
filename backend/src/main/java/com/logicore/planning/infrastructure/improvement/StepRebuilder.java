package com.logicore.planning.infrastructure.improvement;

import com.logicore.planning.domain.valueobject.DecisionReason;
import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.DecisionStep;
import com.logicore.planning.domain.valueobject.PlanningOrder;
import com.logicore.planning.domain.valueobject.RouteConstraints;
import com.logicore.planning.domain.valueobject.RouteMetrics;
import com.logicore.core.domain.model.Vehicle;
import com.logicore.shared.domain.valueobject.GeoCoordinate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Reconstrói DecisionResult a partir de uma permutação de PlanningOrders.
 * Usado após algoritmos de melhoria (2-opt, 3-opt) que alteram a sequência.
 */
public class StepRebuilder {

    public static DecisionResult rebuild(
            List<PlanningOrder> orderedOrders,
            GeoCoordinate depot,
            Vehicle vehicle,
            RouteConstraints constraints,
            DecisionResult original,
            DecisionReason.ReasonType reasonType,
            String reasonSuffix
    ) {
        Map<UUID, DecisionStep> originalStepMap = original.orderedSteps().stream()
                .collect(Collectors.toMap(DecisionStep::orderId, s -> s));

        List<DecisionStep> steps = new ArrayList<>(orderedOrders.size());
        GeoCoordinate current = depot;
        double cumDistance = 0.0;
        int cumTimeMinutes = 0;
        int violations = 0;
        double totalWeight = 0.0;
        LocalTime departure = LocalTime.of(8, 0);

        for (int pos = 1; pos <= orderedOrders.size(); pos++) {
            PlanningOrder order = orderedOrders.get(pos - 1);
            double dist = current.distanceInKmTo(order.deliveryLocation());
            cumDistance += dist;
            int travelMin = (int) Math.ceil(dist / constraints.averageSpeedKmh() * 60.0);
            cumTimeMinutes += travelMin + constraints.stopDurationMinutes();

            LocalTime arrival = departure.plusMinutes(cumTimeMinutes - constraints.stopDurationMinutes());
            boolean inWindow = order.timeWindow() == null || order.timeWindow().contains(arrival);
            if (!inWindow) violations++;
            totalWeight += order.weight().kilograms();

            DecisionStep orig = originalStepMap.get(order.orderId());
            double origDist = orig != null ? orig.distanceFromPreviousKm() : dist;
            double improvement = origDist - dist;

            DecisionReason reason = switch (reasonType) {
                case TWO_OPT_IMPROVED -> DecisionReason.twoOptImproved(improvement, dist);
                case NEAREST_NEIGHBOR -> DecisionReason.nearestNeighbor(dist);
                default -> new DecisionReason(reasonType,
                        String.format("%s: distância %.1f km", reasonSuffix, dist));
            };

            steps.add(new DecisionStep(pos, order.orderId(), order.customerId(),
                    order.customerName(), order.deliveryLocation(), reason,
                    dist, cumDistance, arrival, inWindow,
                    inWindow ? null : "Fora da janela de tempo", order.priority().value()));

            current = order.deliveryLocation();
        }

        double cap = vehicle.getCapacity().kilograms();
        double capPct = cap > 0 ? (totalWeight / cap) * 100.0 : 0.0;
        BigDecimal cost = vehicle.getCostPerKm() == null ? BigDecimal.ZERO
                : vehicle.getCostPerKm().amount().multiply(BigDecimal.valueOf(cumDistance))
                         .setScale(2, RoundingMode.HALF_UP);
        RouteMetrics metrics = new RouteMetrics(cumDistance, cumTimeMinutes, cost,
                totalWeight, capPct, violations, violations == 0);

        return DecisionResult.of(original.strategyType(), original.strategyConfig(), steps, metrics);
    }
}
