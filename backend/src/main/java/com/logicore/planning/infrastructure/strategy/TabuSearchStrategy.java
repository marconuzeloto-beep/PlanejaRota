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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Tabu Search para TSP.
 *
 * Busca local com memória de curto prazo (lista tabu).
 * Em cada iteração, testa todos os vizinhos 2-opt e escolhe o melhor
 * não-tabu (ou que satisfaça critério de aspiração).
 *
 * Parâmetros configuráveis via StrategyConfig:
 *   tabuTenure   (default: 10)
 *   maxIterations (default: 500)
 *
 * Complexidade: O(n² × maxIterations).
 * Qualidade: ~97-99% do ótimo com tenure adequado.
 */
@Component
public class TabuSearchStrategy implements OptimizationStrategy {

    private static final String IDENTIFIER  = "TABU_SEARCH_V1";
    private static final String DESCRIPTION = "Tabu Search — busca local guiada por memória de curto prazo";

    private final ShortestDistanceStrategy seedStrategy;

    public TabuSearchStrategy(ShortestDistanceStrategy seedStrategy) {
        this.seedStrategy = seedStrategy;
    }

    @Override public StrategyType getType()  { return StrategyType.TABU_SEARCH; }
    @Override public String getIdentifier()  { return IDENTIFIER; }
    @Override public String getDescription() { return DESCRIPTION; }

    @Override
    public DecisionResult execute(List<PlanningOrder> orders, Vehicle vehicle,
                                   GeoCoordinate depot, RouteConstraints constraints,
                                   StrategyConfig config) {
        if (orders.size() <= 2) {
            return seedStrategy.execute(orders, vehicle, depot, constraints, config);
        }

        int tenure  = (int) param(config, "tabuTenure", 10.0);
        int maxIter = (int) param(config, "maxIterations", Math.max(500, orders.size() * 20.0));

        DecisionResult initial = seedStrategy.execute(orders, vehicle, depot, constraints, config);
        int n = orders.size();
        int[] current = tourFromResult(initial, orders);
        int[] best = current.clone();
        double bestDist = tourDist(current, orders, depot);

        Deque<int[]> tabuList = new ArrayDeque<>();

        for (int iter = 0; iter < maxIter; iter++) {
            int bestI = -1, bestJ = -1;
            double bestNeighborDist = Double.MAX_VALUE;
            int[] bestNeighbor = null;

            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    int[] neighbor = current.clone();
                    // 2-opt move: reverse segment [i+1, j]
                    int lo = i + 1, hi = j;
                    while (lo < hi) { int t = neighbor[lo]; neighbor[lo] = neighbor[hi]; neighbor[hi] = t; lo++; hi--; }

                    double d = tourDist(neighbor, orders, depot);
                    boolean isTabu = isTabu(tabuList, i, j);

                    // Critério de aspiração: aceitar tabu se melhor que global best
                    if ((!isTabu && d < bestNeighborDist) || (isTabu && d < bestDist)) {
                        bestNeighborDist = d;
                        bestI = i; bestJ = j;
                        bestNeighbor = neighbor;
                    }
                }
            }

            if (bestNeighbor == null) break;

            current = bestNeighbor;
            tabuList.addLast(new int[]{bestI, bestJ});
            if (tabuList.size() > tenure) tabuList.removeFirst();

            if (bestNeighborDist < bestDist) {
                best = current.clone();
                bestDist = bestNeighborDist;
            }
        }

        List<PlanningOrder> orderedOrders = new ArrayList<>(n);
        for (int idx : best) orderedOrders.add(orders.get(idx));

        DecisionResult seed2 = seedStrategy.execute(orders, vehicle, depot, constraints, config);
        return StepRebuilder.rebuild(orderedOrders, depot, vehicle, constraints, seed2,
                DecisionReason.ReasonType.TABU_SEARCH,
                String.format("Tabu Search tenure=%d iter=%d", tenure, maxIter));
    }

    private boolean isTabu(Deque<int[]> tabuList, int i, int j) {
        for (int[] move : tabuList) {
            if ((move[0] == i && move[1] == j) || (move[0] == j && move[1] == i)) return true;
        }
        return false;
    }

    private double tourDist(int[] tour, List<PlanningOrder> orders, GeoCoordinate depot) {
        double total = depot.distanceInKmTo(orders.get(tour[0]).deliveryLocation());
        for (int i = 0; i < tour.length - 1; i++) {
            total += orders.get(tour[i]).deliveryLocation()
                    .distanceInKmTo(orders.get(tour[i + 1]).deliveryLocation());
        }
        return total + orders.get(tour[tour.length - 1]).deliveryLocation().distanceInKmTo(depot);
    }

    private int[] tourFromResult(DecisionResult result, List<PlanningOrder> orders) {
        java.util.Map<java.util.UUID, Integer> idx = new java.util.HashMap<>();
        for (int i = 0; i < orders.size(); i++) idx.put(orders.get(i).orderId(), i);
        return result.orderedSteps().stream().mapToInt(s -> idx.get(s.orderId())).toArray();
    }

    private double param(StrategyConfig config, String key, double def) {
        if (config == null || config.parameters() == null) return def;
        Object v = config.parameters().get(key);
        return v instanceof Number n ? n.doubleValue() : def;
    }
}
