package com.logicore.planning.infrastructure.strategy;

import com.logicore.core.domain.model.Vehicle;
import com.logicore.planning.domain.strategy.OptimizationStrategy;
import com.logicore.planning.domain.strategy.StrategyConfig;
import com.logicore.planning.domain.strategy.StrategyType;
import com.logicore.planning.domain.valueobject.DecisionReason;
import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.PlanningOrder;
import com.logicore.planning.domain.valueobject.RouteConstraints;
import com.logicore.planning.infrastructure.improvement.StepRebuilder;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Simulated Annealing (SA) para TSP.
 *
 * Metaheurística baseada no processo de resfriamento de metais.
 * Aceita soluções piores com probabilidade P = e^(-ΔE/T), onde T decresce.
 *
 * Parâmetros configuráveis via StrategyConfig:
 *   initialTemperature (default: 1000.0)
 *   coolingRate        (default: 0.995)
 *   minTemperature     (default: 0.1)
 *   maxIterations      (default: 10000)
 *   seed               (default: 42)
 *
 * Complexidade: O(n × maxIterations) por execução.
 * Qualidade: ~95-99% do ótimo em instâncias TSP clássicas.
 */
@Component
public class SimulatedAnnealingStrategy implements OptimizationStrategy {

    private static final String IDENTIFIER  = "SIMULATED_ANNEALING_V1";
    private static final String DESCRIPTION = "Simulated Annealing — exploração probabilística do espaço de soluções";

    private final ShortestDistanceStrategy seedStrategy;

    public SimulatedAnnealingStrategy(ShortestDistanceStrategy seedStrategy) {
        this.seedStrategy = seedStrategy;
    }

    @Override public StrategyType getType()  { return StrategyType.SIMULATED_ANNEALING; }
    @Override public String getIdentifier()  { return IDENTIFIER; }
    @Override public String getDescription() { return DESCRIPTION; }

    @Override
    public DecisionResult execute(List<PlanningOrder> orders, Vehicle vehicle,
                                   GeoCoordinate depot, RouteConstraints constraints,
                                   StrategyConfig config) {
        if (orders.size() <= 2) {
            return seedStrategy.execute(orders, vehicle, depot, constraints, config);
        }

        double T0      = param(config, "initialTemperature", 1000.0);
        double alpha   = param(config, "coolingRate", 0.995);
        double Tmin    = param(config, "minTemperature", 0.1);
        int maxIter    = (int) param(config, "maxIterations", Math.max(10000, orders.size() * 200.0));
        long seed      = (long) param(config, "seed", 42.0);
        Random rng     = new Random(seed);

        // Solução inicial via NN
        DecisionResult initial = seedStrategy.execute(orders, vehicle, depot, constraints, config);
        int n = orders.size();
        int[] current = tourFromResult(initial, orders);
        int[] best = current.clone();
        double currentDist = tourDist(current, orders, depot);
        double bestDist = currentDist;
        double T = T0;
        double finalTemp = T;

        for (int iter = 0; iter < maxIter && T > Tmin; iter++) {
            // Perturbação: swap aleatório de dois clientes
            int i = rng.nextInt(n);
            int j = rng.nextInt(n);
            while (j == i) j = rng.nextInt(n);

            int[] neighbor = current.clone();
            int tmp = neighbor[i]; neighbor[i] = neighbor[j]; neighbor[j] = tmp;

            double neighborDist = tourDist(neighbor, orders, depot);
            double delta = neighborDist - currentDist;

            if (delta < 0 || rng.nextDouble() < Math.exp(-delta / T)) {
                current = neighbor;
                currentDist = neighborDist;
                if (currentDist < bestDist) {
                    best = current.clone();
                    bestDist = currentDist;
                }
            }
            T *= alpha;
            if (iter == maxIter - 1) finalTemp = T;
        }

        List<PlanningOrder> orderedOrders = new ArrayList<>(n);
        for (int idx : best) orderedOrders.add(orders.get(idx));
        double finalTemperature = finalTemp;

        DecisionResult seed2 = seedStrategy.execute(orders, vehicle, depot, constraints, config);
        return StepRebuilder.rebuild(orderedOrders, depot, vehicle, constraints, seed2,
                DecisionReason.ReasonType.SIMULATED_ANNEALING,
                String.format("SA T=%.2f→%.4f iter=%d", T0, finalTemperature, maxIter));
    }

    private double tourDist(int[] tour, List<PlanningOrder> orders, GeoCoordinate depot) {
        double total = depot.distanceInKmTo(orders.get(tour[0]).deliveryLocation());
        for (int i = 0; i < tour.length - 1; i++) {
            total += orders.get(tour[i]).deliveryLocation()
                    .distanceInKmTo(orders.get(tour[i + 1]).deliveryLocation());
        }
        total += orders.get(tour[tour.length - 1]).deliveryLocation().distanceInKmTo(depot);
        return total;
    }

    private int[] tourFromResult(DecisionResult result, List<PlanningOrder> orders) {
        java.util.Map<java.util.UUID, Integer> idx = new java.util.HashMap<>();
        for (int i = 0; i < orders.size(); i++) idx.put(orders.get(i).orderId(), i);
        return result.orderedSteps().stream().mapToInt(s -> idx.get(s.orderId())).toArray();
    }

    private double param(StrategyConfig config, String key, double def) {
        if (config == null || config.params() == null) return def;
        Object v = config.params().get(key);
        return v instanceof Number n ? n.doubleValue() : def;
    }
}
