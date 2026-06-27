package com.logicore.planning.infrastructure.improvement;

import com.logicore.core.domain.model.Vehicle;
import com.logicore.planning.domain.distance.DistanceMatrix;
import com.logicore.planning.domain.valueobject.DecisionReason;
import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.PlanningOrder;
import com.logicore.planning.domain.valueobject.RouteConstraints;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Algoritmo de melhoria local 2-Opt.
 *
 * Complexidade: O(n²) por iteração, O(n² × k) total onde k = número de iterações.
 *
 * Para cada par de arestas (i→i+1) e (j→j+1), verifica se trocá-las pela conexão
 * cruzada (i→j) e (i+1→j+1) reduz a distância total. Se sim, reverte o segmento
 * entre i+1 e j.
 *
 * Garantia: resultado ≤ distância inicial (nunca piora).
 */
@Component
public class TwoOptImprover {

    private static final int MAX_ITERATIONS = 100;

    /**
     * Aplica 2-opt ao DecisionResult e retorna versão melhorada.
     *
     * @return par [resultado melhorado, melhoria em km]
     */
    public TwoOptResult improve(DecisionResult initial, List<PlanningOrder> orders,
                                 GeoCoordinate depot, Vehicle vehicle,
                                 RouteConstraints constraints, DistanceMatrix matrix) {
        if (orders.size() <= 2) {
            return new TwoOptResult(initial, 0.0);
        }

        // tour: índices 0..n-1 representando a sequência de clientes
        int n = orders.size();
        int[] tour = new int[n];
        for (int i = 0; i < n; i++) tour[i] = i;

        // Mapear orderId para posição no array orders para acesso a matrix
        // matrix index: 0=depot, 1..n = orders[0..n-1]

        double bestDist = computeTourDist(tour, matrix);
        boolean improved = true;
        int iterations = 0;

        while (improved && iterations < MAX_ITERATIONS) {
            improved = false;
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 2; j < n; j++) {
                    // Calcula ganho de reverter segmento tour[i+1..j]
                    int nodeI  = tour[i];
                    int nodeI1 = tour[i + 1];
                    int nodeJ  = tour[j];
                    int nodeJ1 = (j + 1 < n) ? tour[j + 1] : -1; // -1 = depot

                    double distI   = matrix.distance(nodeI  + 1, nodeI1 + 1);
                    double distJ   = nodeJ1 >= 0 ? matrix.distance(nodeJ  + 1, nodeJ1 + 1)
                                                 : matrix.distance(nodeJ  + 1, 0);
                    double newI    = matrix.distance(nodeI  + 1, nodeJ  + 1);
                    double newJ    = nodeJ1 >= 0 ? matrix.distance(nodeI1 + 1, nodeJ1 + 1)
                                                 : matrix.distance(nodeI1 + 1, 0);

                    double gain = (distI + distJ) - (newI + newJ);
                    if (gain > 1e-6) {
                        reverse(tour, i + 1, j);
                        bestDist -= gain;
                        improved = true;
                    }
                }
            }
            iterations++;
        }

        double initialDist = initial.metrics().totalDistanceKm();

        // Reconstruct ordered PlanningOrders from tour
        List<PlanningOrder> orderedOrders = new ArrayList<>(n);
        for (int idx : tour) orderedOrders.add(orders.get(idx));

        DecisionResult improved2 = StepRebuilder.rebuild(
                orderedOrders, depot, vehicle, constraints, initial,
                DecisionReason.ReasonType.TWO_OPT_IMPROVED, "2-Opt"
        );

        double improvementKm = Math.max(0, initialDist - improved2.metrics().totalDistanceKm());
        return new TwoOptResult(improved2, improvementKm);
    }

    private void reverse(int[] arr, int from, int to) {
        while (from < to) {
            int tmp = arr[from]; arr[from] = arr[to]; arr[to] = tmp;
            from++; to--;
        }
    }

    private double computeTourDist(int[] tour, DistanceMatrix matrix) {
        double total = matrix.distance(0, tour[0] + 1);
        for (int i = 0; i < tour.length - 1; i++) {
            total += matrix.distance(tour[i] + 1, tour[i + 1] + 1);
        }
        total += matrix.distance(tour[tour.length - 1] + 1, 0);
        return total;
    }

    public record TwoOptResult(DecisionResult result, double improvementKm) {}
}
