package com.logicore.planning.domain.distance;

import com.logicore.shared.domain.valueobject.GeoCoordinate;

import java.util.List;

/**
 * Matriz de distâncias pré-computada.
 * Índice 0 = depot, 1..n = clientes na ordem fornecida.
 * Imutável — construída uma vez e compartilhada por toda execução do pipeline.
 */
public record DistanceMatrix(
        double[][] distances,
        double[][] durations,
        List<GeoCoordinate> nodes
) {
    public DistanceMatrix {
        nodes = List.copyOf(nodes);
    }

    public double distance(int from, int to) { return distances[from][to]; }
    public double duration(int from, int to) { return durations[from][to]; }

    /** Número de nós incluindo depot. */
    public int size() { return nodes.size(); }

    /** Número de clientes (sem depot). */
    public int customerCount() { return nodes.size() - 1; }

    /**
     * Custo total de um tour representado como permutação de índices de clientes (1..n).
     * Inclui retorno ao depot.
     */
    public double tourDistance(int[] tour) {
        double total = distances[0][tour[0]];
        for (int i = 0; i < tour.length - 1; i++) {
            total += distances[tour[i]][tour[i + 1]];
        }
        total += distances[tour[tour.length - 1]][0];
        return total;
    }

    /** Tour canônico: [1, 2, 3, ..., n] */
    public int[] identityTour() {
        int n = customerCount();
        int[] tour = new int[n];
        for (int i = 0; i < n; i++) tour[i] = i + 1;
        return tour;
    }
}
