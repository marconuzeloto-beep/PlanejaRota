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
 * Genetic Algorithm (GA) para TSP.
 *
 * Algoritmo evolutivo baseado em seleção natural.
 * Representação: permutação de clientes.
 * Operadores:
 *   - Seleção: torneio de tamanho 3
 *   - Crossover: Order Crossover (OX)
 *   - Mutação: swap de dois genes
 *   - Elitismo: melhor indivíduo sempre sobrevive
 *
 * Parâmetros configuráveis:
 *   populationSize  (default: max(50, 10*n))
 *   generations     (default: max(200, 20*n))
 *   mutationRate    (default: 0.02)
 *   seed            (default: 42)
 *
 * Complexidade: O(populationSize × generations × n).
 * Qualidade: ~95-99% do ótimo, excelente para instâncias grandes.
 */
@Component
public class GeneticAlgorithmStrategy implements OptimizationStrategy {

    private static final String IDENTIFIER  = "GENETIC_ALGORITHM_V1";
    private static final String DESCRIPTION = "Genetic Algorithm — evolução de população de soluções";

    private final ShortestDistanceStrategy seedStrategy;

    public GeneticAlgorithmStrategy(ShortestDistanceStrategy seedStrategy) {
        this.seedStrategy = seedStrategy;
    }

    @Override public StrategyType getType()  { return StrategyType.GENETIC_ALGORITHM; }
    @Override public String getIdentifier()  { return IDENTIFIER; }
    @Override public String getDescription() { return DESCRIPTION; }

    @Override
    public DecisionResult execute(List<PlanningOrder> orders, Vehicle vehicle,
                                   GeoCoordinate depot, RouteConstraints constraints,
                                   StrategyConfig config) {
        if (orders.size() <= 2) {
            return seedStrategy.execute(orders, vehicle, depot, constraints, config);
        }

        int n          = orders.size();
        int popSize    = (int) param(config, "populationSize", Math.max(50, n * 10.0));
        int gens       = (int) param(config, "generations", Math.max(200, n * 20.0));
        double mutRate = param(config, "mutationRate", 0.02);
        long seed      = (long) param(config, "seed", 42.0);
        Random rng     = new Random(seed);

        // Inicializar população: primeiro individuo = NN, resto = aleatório
        int[][] population = new int[popSize][n];
        DecisionResult initial = seedStrategy.execute(orders, vehicle, depot, constraints, config);
        population[0] = tourFromResult(initial, orders);
        for (int i = 1; i < popSize; i++) {
            population[i] = randomTour(n, rng);
        }

        int[] best = population[0].clone();
        double bestDist = tourDist(best, orders, depot);
        int bestGeneration = 0;

        for (int gen = 0; gen < gens; gen++) {
            int[][] newPop = new int[popSize][n];
            // Elitismo: copia melhor
            newPop[0] = best.clone();

            for (int i = 1; i < popSize; i++) {
                int[] parent1 = tournamentSelect(population, orders, depot, 3, rng);
                int[] parent2 = tournamentSelect(population, orders, depot, 3, rng);
                int[] child = orderCrossover(parent1, parent2, rng, n);
                if (rng.nextDouble() < mutRate) swapMutation(child, rng);
                newPop[i] = child;

                double d = tourDist(child, orders, depot);
                if (d < bestDist) {
                    best = child.clone();
                    bestDist = d;
                    bestGeneration = gen;
                }
            }
            population = newPop;
        }

        List<PlanningOrder> orderedOrders = new ArrayList<>(n);
        for (int idx : best) orderedOrders.add(orders.get(idx));
        final int finalGen = bestGeneration;
        final double fitness = 1.0 / bestDist;

        DecisionResult seed2 = seedStrategy.execute(orders, vehicle, depot, constraints, config);
        return StepRebuilder.rebuild(orderedOrders, depot, vehicle, constraints, seed2,
                DecisionReason.ReasonType.GENETIC_SELECTION,
                String.format("GA gen=%d fitness=%.4f", finalGen, fitness));
    }

    private int[] tournamentSelect(int[][] pop, List<PlanningOrder> orders,
                                    GeoCoordinate depot, int k, Random rng) {
        int[] best = null;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < k; i++) {
            int[] candidate = pop[rng.nextInt(pop.length)];
            double d = tourDist(candidate, orders, depot);
            if (d < bestDist) { bestDist = d; best = candidate; }
        }
        return best;
    }

    private int[] orderCrossover(int[] p1, int[] p2, Random rng, int n) {
        int start = rng.nextInt(n);
        int end = rng.nextInt(n);
        if (start > end) { int t = start; start = end; end = t; }
        boolean[] inSegment = new boolean[n];
        int[] child = new int[n];
        Arrays.fill(child, -1);
        for (int i = start; i <= end; i++) { child[i] = p1[i]; inSegment[p1[i]] = true; }
        int pos = (end + 1) % n;
        for (int i = 0; i < n; i++) {
            int gene = p2[(end + 1 + i) % n];
            if (!inSegment[gene]) { child[pos] = gene; pos = (pos + 1) % n; }
        }
        return child;
    }

    private void swapMutation(int[] tour, Random rng) {
        int i = rng.nextInt(tour.length);
        int j = rng.nextInt(tour.length);
        int t = tour[i]; tour[i] = tour[j]; tour[j] = t;
    }

    private int[] randomTour(int n, Random rng) {
        int[] tour = new int[n];
        for (int i = 0; i < n; i++) tour[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int t = tour[i]; tour[i] = tour[j]; tour[j] = t;
        }
        return tour;
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
        if (config == null || config.params() == null) return def;
        Object v = config.params().get(key);
        return v instanceof Number nb ? nb.doubleValue() : def;
    }
}
