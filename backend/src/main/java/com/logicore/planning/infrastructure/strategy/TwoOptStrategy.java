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
import java.util.List;

/**
 * 2-Opt como estratégia autônoma.
 *
 * Usa Nearest Neighbor como solução inicial e aplica 2-opt completo.
 *
 * Complexidade: O(n² × k) onde k = iterações até convergência.
 * Qualidade: ~95-98% do ótimo em instâncias euclidianas médias.
 */
@Component
public class TwoOptStrategy implements OptimizationStrategy {

    private static final String IDENTIFIER  = "TWO_OPT_V1";
    private static final String DESCRIPTION = "2-Opt — melhoria local de arestas cruzadas";
    private static final int MAX_ITERATIONS = 200;

    private final ShortestDistanceStrategy seedStrategy;

    public TwoOptStrategy(ShortestDistanceStrategy seedStrategy) {
        this.seedStrategy = seedStrategy;
    }

    @Override public StrategyType getType()  { return StrategyType.TWO_OPT; }
    @Override public String getIdentifier()  { return IDENTIFIER; }
    @Override public String getDescription() { return DESCRIPTION; }

    @Override
    public DecisionResult execute(List<PlanningOrder> orders, Vehicle vehicle,
                                   GeoCoordinate depot, RouteConstraints constraints,
                                   StrategyConfig config) {
        // Solução inicial via Nearest Neighbor
        DecisionResult initial = seedStrategy.execute(orders, vehicle, depot, constraints, config);

        if (orders.size() <= 2) return initial;

        // Extrai tour como array de índices (0 = primeiro cliente, etc.)
        int n = orders.size();
        int[] tour = initialTourFromResult(initial, orders);

        boolean improved = true;
        int iter = 0;
        while (improved && iter < MAX_ITERATIONS) {
            improved = false;
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 2; j < n; j++) {
                    double d1 = dist(depot, orders, tour, i, i + 1)
                              + dist(depot, orders, tour, j, (j + 1) % n);
                    double d2 = dist(depot, orders, tour, i, j)
                              + dist(depot, orders, tour, i + 1, (j + 1) % n);
                    if (d1 - d2 > 1e-6) {
                        reverse(tour, i + 1, j);
                        improved = true;
                    }
                }
            }
            iter++;
        }

        List<PlanningOrder> orderedOrders = new ArrayList<>(n);
        for (int idx : tour) orderedOrders.add(orders.get(idx));

        return StepRebuilder.rebuild(orderedOrders, depot, vehicle, constraints, initial,
                DecisionReason.ReasonType.TWO_OPT_IMPROVED, "2-Opt");
    }

    private int[] initialTourFromResult(DecisionResult result, List<PlanningOrder> orders) {
        java.util.Map<java.util.UUID, Integer> orderIndex = new java.util.HashMap<>();
        for (int i = 0; i < orders.size(); i++) orderIndex.put(orders.get(i).orderId(), i);
        return result.orderedSteps().stream()
                .mapToInt(s -> orderIndex.get(s.orderId()))
                .toArray();
    }

    private double dist(GeoCoordinate depot, List<PlanningOrder> orders, int[] tour, int from, int to) {
        GeoCoordinate a = from == -1 ? depot : orders.get(tour[from]).deliveryLocation();
        GeoCoordinate b = to >= orders.size() ? depot : orders.get(tour[to % orders.size()]).deliveryLocation();
        return a.distanceInKmTo(b);
    }

    private void reverse(int[] arr, int from, int to) {
        while (from < to) { int t = arr[from]; arr[from] = arr[to]; arr[to] = t; from++; to--; }
    }
}
