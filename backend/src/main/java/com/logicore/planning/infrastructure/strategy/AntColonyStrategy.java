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
import java.util.Random;

/**
 * Ant Colony Optimization (ACO) para TSP.
 *
 * Metaheurística inspirada no comportamento de formigas depositando feromônio.
 * Formigas constroem soluções probabilisticamente guiadas por:
 *   P(i→j) ∝ τ[i][j]^α × η[i][j]^β
 *   onde η[i][j] = 1/d[i][j] (visibilidade)
 *
 * Atualização de feromônio:
 *   τ[i][j] ← (1-ρ)×τ[i][j] + Σ ΔQ/L_k
 *
 * Parâmetros configuráveis:
 *   nAnts       (default: max(10, n))
 *   alpha       (default: 1.0)
 *   beta        (default: 5.0)
 *   evaporation (default: 0.1)
 *   Q           (default: 100.0)
 *   iterations  (default: max(100, 10*n))
 *   seed        (default: 42)
 *
 * Complexidade: O(nAnts × n² × iterations).
 * Qualidade: ~95-99% do ótimo. Excelente em problemas com clusters naturais.
 */
@Component
public class AntColonyStrategy implements OptimizationStrategy {

    private static final String IDENTIFIER  = "ANT_COLONY_V1";
    private static final String DESCRIPTION = "Ant Colony Optimization — feromônio guia construção de rotas";

    private final ShortestDistanceStrategy seedStrategy;

    public AntColonyStrategy(ShortestDistanceStrategy seedStrategy) {
        this.seedStrategy = seedStrategy;
    }

    @Override public StrategyType getType()  { return StrategyType.ANT_COLONY_OPTIMIZATION; }
    @Override public String getIdentifier()  { return IDENTIFIER; }
    @Override public String getDescription() { return DESCRIPTION; }

    @Override
    public DecisionResult execute(List<PlanningOrder> orders, Vehicle vehicle,
                                   GeoCoordinate depot, RouteConstraints constraints,
                                   StrategyConfig config) {
        if (orders.size() <= 2) {
            return seedStrategy.execute(orders, vehicle, depot, constraints, config);
        }

        int n       = orders.size();
        int nAnts   = (int) param(config, "nAnts", Math.max(10, (double) n));
        double alpha= param(config, "alpha", 1.0);
        double beta = param(config, "beta", 5.0);
        double rho  = param(config, "evaporation", 0.1);
        double Q    = param(config, "Q", 100.0);
        int iters   = (int) param(config, "iterations", Math.max(100, n * 10.0));
        long seed   = (long) param(config, "seed", 42.0);
        Random rng  = new Random(seed);

        // Matriz de distâncias (inclui depot como nó 0)
        int total = n + 1;
        double[][] dist = new double[total][total];
        double[][] pheromone = new double[total][total];
        double initPheromone = 1.0;

        for (int i = 0; i < total; i++) {
            for (int j = 0; j < total; j++) {
                if (i == j) { dist[i][j] = 0; pheromone[i][j] = 0; continue; }
                GeoCoordinate a = i == 0 ? depot : orders.get(i - 1).deliveryLocation();
                GeoCoordinate b = j == 0 ? depot : orders.get(j - 1).deliveryLocation();
                dist[i][j] = a.distanceInKmTo(b);
                pheromone[i][j] = initPheromone;
            }
        }

        int[] bestTour = null;
        double bestDist = Double.MAX_VALUE;
        double bestPheromoneUsed = initPheromone;

        for (int iter = 0; iter < iters; iter++) {
            int[][] antTours = new int[nAnts][n];
            double[] antDists = new double[nAnts];

            for (int ant = 0; ant < nAnts; ant++) {
                boolean[] visited = new boolean[total];
                visited[0] = true;
                antTours[ant][0] = 1 + rng.nextInt(n); // começa em cliente aleatório
                visited[antTours[ant][0]] = true;

                for (int step = 1; step < n; step++) {
                    int current = antTours[ant][step - 1];
                    double[] prob = new double[total];
                    double sum = 0;
                    for (int j = 1; j < total; j++) {
                        if (!visited[j] && dist[current][j] > 0) {
                            prob[j] = Math.pow(pheromone[current][j], alpha)
                                    * Math.pow(1.0 / dist[current][j], beta);
                            sum += prob[j];
                        }
                    }
                    double rand = rng.nextDouble() * sum;
                    double cumul = 0;
                    int chosen = -1;
                    for (int j = 1; j < total; j++) {
                        if (!visited[j]) {
                            cumul += prob[j];
                            if (cumul >= rand) { chosen = j; break; }
                        }
                    }
                    if (chosen == -1) { // fallback: primeiro não visitado
                        for (int j = 1; j < total; j++) if (!visited[j]) { chosen = j; break; }
                    }
                    antTours[ant][step] = chosen;
                    visited[chosen] = true;
                }

                // Calcular distância do tour desta formiga
                double d = dist[0][antTours[ant][0]];
                for (int s = 0; s < n - 1; s++) d += dist[antTours[ant][s]][antTours[ant][s + 1]];
                d += dist[antTours[ant][n - 1]][0];
                antDists[ant] = d;

                if (d < bestDist) {
                    bestDist = d;
                    bestTour = antTours[ant].clone();
                    bestPheromoneUsed = pheromone[antTours[ant][0]][antTours[ant].length > 1 ? antTours[ant][1] : 0];
                }
            }

            // Evaporação
            for (int i = 0; i < total; i++)
                for (int j = 0; j < total; j++)
                    pheromone[i][j] *= (1 - rho);

            // Depósito
            for (int ant = 0; ant < nAnts; ant++) {
                double delta = Q / antDists[ant];
                pheromone[0][antTours[ant][0]] += delta;
                for (int s = 0; s < n - 1; s++) {
                    pheromone[antTours[ant][s]][antTours[ant][s + 1]] += delta;
                    pheromone[antTours[ant][s + 1]][antTours[ant][s]] += delta;
                }
                pheromone[antTours[ant][n - 1]][0] += delta;
            }
        }

        // Converter tour (índices 1..n) para lista de PlanningOrders (0..n-1)
        List<PlanningOrder> orderedOrders = new ArrayList<>(n);
        for (int nodeIdx : bestTour) orderedOrders.add(orders.get(nodeIdx - 1));

        DecisionResult seed2 = seedStrategy.execute(orders, vehicle, depot, constraints, config);
        double finalPheromone = bestPheromoneUsed;
        return StepRebuilder.rebuild(orderedOrders, depot, vehicle, constraints, seed2,
                DecisionReason.ReasonType.ACO_PHEROMONE,
                String.format("ACO ants=%d iters=%d ρ=%.2f", nAnts, iters, rho));
    }

    private double param(StrategyConfig config, String key, double def) {
        if (config == null || config.params() == null) return def;
        Object v = config.params().get(key);
        return v instanceof Number n ? n.doubleValue() : def;
    }
}
