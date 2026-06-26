package com.planejarota.domain.service;

import com.planejarota.domain.model.route.Route;
import com.planejarota.domain.model.route.RouteStop;
import com.planejarota.domain.valueobject.GeoCoordinate;

import java.util.*;

/**
 * Domain Service para otimização de rotas.
 *
 * Implementa Nearest Neighbor Heuristic + 2-opt local search.
 * É um Domain Service (não entidade) porque opera sobre múltiplas entidades
 * e não pertence a nenhuma delas individualmente.
 *
 * Complexidade: O(n²) para NN, O(n²) por iteração para 2-opt.
 * Adequado para até ~100 paradas sem degradação perceptível.
 *
 * ADR-001: Esta implementação é substituível via RouteOptimizationPort.
 */
public class RouteOptimizationService {

    private static final int MAX_TWO_OPT_ITERATIONS = 100;

    /**
     * Calcula a ordem ótima das paradas usando Nearest Neighbor + 2-opt.
     * Retorna a lista de IDs de paradas na ordem recomendada.
     */
    public List<UUID> optimize(Route route) {
        List<RouteStop> stops = new ArrayList<>(route.getStops());
        if (stops.size() <= 1) {
            return stops.stream().map(RouteStop::getId).toList();
        }

        List<RouteStop> ordered = nearestNeighbor(route.getDepotLocation(), stops);
        ordered = twoOptImprovement(ordered, route.getDepotLocation());

        return ordered.stream().map(RouteStop::getId).toList();
    }

    private List<RouteStop> nearestNeighbor(GeoCoordinate depot, List<RouteStop> stops) {
        List<RouteStop> unvisited = new ArrayList<>(stops);
        List<RouteStop> result = new ArrayList<>();

        GeoCoordinate current = depot;
        while (!unvisited.isEmpty()) {
            RouteStop nearest = findNearest(current, unvisited);
            result.add(nearest);
            current = nearest.getLocation();
            unvisited.remove(nearest);
        }
        return result;
    }

    private RouteStop findNearest(GeoCoordinate from, List<RouteStop> candidates) {
        return candidates.stream()
                .min(Comparator.comparingDouble(s -> from.distanceInKmTo(s.getLocation())))
                .orElseThrow();
    }

    /**
     * 2-opt: tenta reverter segmentos da rota para reduzir distância total.
     * Continua até não haver melhoria ou atingir o limite de iterações.
     */
    private List<RouteStop> twoOptImprovement(List<RouteStop> stops, GeoCoordinate depot) {
        List<RouteStop> best = new ArrayList<>(stops);
        boolean improved = true;
        int iterations = 0;

        while (improved && iterations < MAX_TWO_OPT_ITERATIONS) {
            improved = false;
            iterations++;
            for (int i = 0; i < best.size() - 1; i++) {
                for (int k = i + 1; k < best.size(); k++) {
                    List<RouteStop> candidate = twoOptSwap(best, i, k);
                    if (totalDistance(candidate, depot) < totalDistance(best, depot)) {
                        best = candidate;
                        improved = true;
                    }
                }
            }
        }
        return best;
    }

    private List<RouteStop> twoOptSwap(List<RouteStop> stops, int i, int k) {
        List<RouteStop> result = new ArrayList<>(stops.subList(0, i));
        List<RouteStop> reversed = new ArrayList<>(stops.subList(i, k + 1));
        Collections.reverse(reversed);
        result.addAll(reversed);
        result.addAll(stops.subList(k + 1, stops.size()));
        return result;
    }

    private double totalDistance(List<RouteStop> stops, GeoCoordinate depot) {
        if (stops.isEmpty()) return 0;
        double total = depot.distanceInKmTo(stops.get(0).getLocation());
        for (int i = 0; i < stops.size() - 1; i++) {
            total += stops.get(i).getLocation().distanceInKmTo(stops.get(i + 1).getLocation());
        }
        total += stops.get(stops.size() - 1).getLocation().distanceInKmTo(depot);
        return total;
    }
}
