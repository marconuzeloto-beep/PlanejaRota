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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Clarke-Wright Savings Algorithm (1964).
 *
 * Heurística construtiva clássica para VRP.
 * Começa com N rotas independentes (depot→cliente→depot) e as funde
 * em ordem decrescente de economia: s(i,j) = d(0,i) + d(0,j) - d(i,j).
 *
 * Complexidade: O(n² log n) — merge sort dos savings + iteração.
 * Qualidade: ~85-90% do ótimo em instâncias típicas.
 */
@Component
public class ClarkeWrightSavingsStrategy implements OptimizationStrategy {

    private static final String IDENTIFIER  = "CLARKE_WRIGHT_V1";
    private static final String DESCRIPTION = "Clarke-Wright Savings — fusão de rotas por maior economia";

    @Override public StrategyType getType()  { return StrategyType.CLARKE_WRIGHT_SAVINGS; }
    @Override public String getIdentifier()  { return IDENTIFIER; }
    @Override public String getDescription() { return DESCRIPTION; }

    @Override
    public DecisionResult execute(List<PlanningOrder> orders, Vehicle vehicle,
                                   GeoCoordinate depot, RouteConstraints constraints,
                                   StrategyConfig config) {
        int n = orders.size();
        double[] depotDist = new double[n];
        for (int i = 0; i < n; i++) {
            depotDist[i] = depot.distanceInKmTo(orders.get(i).deliveryLocation());
        }

        // Calcular savings s(i,j) = d(0,i) + d(0,j) - d(i,j)
        record Saving(int i, int j, double value) {}
        List<Saving> savings = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double dij = orders.get(i).deliveryLocation()
                        .distanceInKmTo(orders.get(j).deliveryLocation());
                double s = depotDist[i] + depotDist[j] - dij;
                savings.add(new Saving(i, j, s));
            }
        }
        savings.sort(Comparator.comparingDouble(Saving::value).reversed());

        // Cada cliente começa em rota própria [i]
        // route[i] = LinkedList de índices representando a rota do cliente i
        @SuppressWarnings("unchecked")
        LinkedList<Integer>[] routes = new LinkedList[n];
        int[] routeOf = new int[n]; // routeOf[i] = qual rota o cliente i pertence
        for (int i = 0; i < n; i++) {
            routes[i] = new LinkedList<>();
            routes[i].add(i);
            routeOf[i] = i;
        }

        double capacityKg = vehicle.getCapacity().kilograms();
        double[] routeWeight = new double[n];
        for (int i = 0; i < n; i++) {
            routeWeight[i] = orders.get(i).weight().kilograms();
        }

        // Fundir rotas em ordem decrescente de savings
        for (Saving s : savings) {
            int ri = routeOf[s.i()];
            int rj = routeOf[s.j()];
            if (ri == rj) continue; // já na mesma rota
            if (routes[ri] == null || routes[rj] == null) continue;

            // i deve ser o último da rota ri e j deve ser o primeiro da rota rj (ou vice-versa)
            boolean iIsLast  = routes[ri].getLast().equals(s.i());
            boolean jIsFirst = routes[rj].getFirst().equals(s.j());
            boolean jIsLast  = routes[rj].getLast().equals(s.j());
            boolean iIsFirst = routes[ri].getFirst().equals(s.i());

            if (!iIsLast && !jIsFirst && !iIsFirst && !jIsLast) continue;

            double merged = routeWeight[ri] + routeWeight[rj];
            if (merged > capacityKg) continue;

            // Merge: ri + rj
            if (iIsLast && jIsFirst) {
                routes[ri].addAll(routes[rj]);
            } else if (jIsLast && iIsFirst) {
                routes[rj].addAll(routes[ri]);
                routes[ri] = routes[rj];
            } else {
                continue;
            }

            routeWeight[ri] = merged;
            for (int node : routes[ri]) routeOf[node] = ri;
            routes[rj] = null;
        }

        // Coletar rota final (primeira rota não-null com todos os clientes)
        List<Integer> finalTour = new ArrayList<>();
        boolean[] visited = new boolean[n];
        for (int i = 0; i < n; i++) {
            if (routes[i] != null) {
                for (int idx : routes[i]) {
                    if (!visited[idx]) {
                        finalTour.add(idx);
                        visited[idx] = true;
                    }
                }
            }
        }
        // Adicionar clientes não incluídos (fallback)
        for (int i = 0; i < n; i++) {
            if (!visited[i]) finalTour.add(i);
        }

        return buildResult(finalTour, orders, depot, vehicle, constraints, config, depotDist, savings);
    }

    private DecisionResult buildResult(List<Integer> tour, List<PlanningOrder> orders,
                                        GeoCoordinate depot, Vehicle vehicle,
                                        RouteConstraints constraints, StrategyConfig config,
                                        double[] depotDist, List<?> savings) {
        List<DecisionStep> steps = new ArrayList<>();
        GeoCoordinate current = depot;
        double cumDist = 0; int cumTime = 0; double totalWeight = 0; int violations = 0;
        LocalTime departure = LocalTime.of(8, 0);

        for (int pos = 1; pos <= tour.size(); pos++) {
            int idx = tour.get(pos - 1);
            PlanningOrder order = orders.get(idx);
            double dist = current.distanceInKmTo(order.deliveryLocation());
            cumDist += dist;
            int travelMin = (int) Math.ceil(dist / constraints.averageSpeedKmh() * 60.0);
            cumTime += travelMin + constraints.stopDurationMinutes();
            LocalTime arrival = departure.plusMinutes(cumTime - constraints.stopDurationMinutes());
            boolean inWindow = order.timeWindow() == null || order.timeWindow().contains(arrival);
            if (!inWindow) violations++;
            totalWeight += order.weight().kilograms();

            double saving = depotDist[idx] + (pos > 1 ? depotDist[tour.get(pos - 2)] : 0) - dist;
            steps.add(new DecisionStep(pos, order.orderId(), order.customerId(), order.customerName(),
                    order.deliveryLocation(), DecisionReason.savingsAlgorithm(Math.max(0, saving), dist),
                    dist, cumDist, arrival, inWindow,
                    inWindow ? null : "Fora da janela de tempo", order.priority().value()));
            current = order.deliveryLocation();
        }

        double cap = vehicle.getCapacity().kilograms();
        double capPct = cap > 0 ? (totalWeight / cap) * 100.0 : 0.0;
        BigDecimal cost = vehicle.getCostPerKm() == null ? BigDecimal.ZERO
                : vehicle.getCostPerKm().amount().multiply(BigDecimal.valueOf(cumDist))
                         .setScale(2, RoundingMode.HALF_UP);
        RouteMetrics metrics = new RouteMetrics(cumDist, cumTime, cost, totalWeight, capPct, violations, violations == 0);
        return DecisionResult.of(StrategyType.CLARKE_WRIGHT_SAVINGS, config, steps, metrics);
    }
}
