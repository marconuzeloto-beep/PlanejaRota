package com.logicore.planning.domain.distance;

import com.logicore.shared.domain.valueobject.GeoCoordinate;

import java.util.List;

/**
 * Porta para provedores de distância real entre coordenadas.
 * Permite trocar Haversine por OSRM, GraphHopper ou Google Maps sem alterar algoritmos.
 */
public interface DistanceProvider {

    String getProviderName();

    /**
     * Distância em km entre dois pontos.
     */
    double distanceKm(GeoCoordinate from, GeoCoordinate to);

    /**
     * Duração estimada em minutos entre dois pontos dado velocidade média.
     */
    double durationMinutes(GeoCoordinate from, GeoCoordinate to, double avgSpeedKmh);

    /**
     * Constrói uma matriz de distância pré-computada.
     * Índice 0 = depot, 1..n = orders na ordem da lista.
     */
    default DistanceMatrix buildMatrix(GeoCoordinate depot, List<GeoCoordinate> locations, double avgSpeedKmh) {
        int n = locations.size() + 1;
        double[][] dist = new double[n][n];
        double[][] dur = new double[n][n];

        List<GeoCoordinate> nodes = new java.util.ArrayList<>();
        nodes.add(depot);
        nodes.addAll(locations);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                dist[i][j] = distanceKm(nodes.get(i), nodes.get(j));
                dur[i][j] = durationMinutes(nodes.get(i), nodes.get(j), avgSpeedKmh);
            }
        }
        return new DistanceMatrix(dist, dur, nodes);
    }
}
