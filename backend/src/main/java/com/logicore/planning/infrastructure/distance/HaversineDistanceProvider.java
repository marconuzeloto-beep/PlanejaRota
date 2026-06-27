package com.logicore.planning.infrastructure.distance;

import com.logicore.planning.domain.distance.DistanceProvider;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import org.springframework.stereotype.Component;

/**
 * Provedor de distância via fórmula de Haversine.
 * Precisão < 0.5% em distâncias urbanas — adequado para otimização logística.
 * Implementações futuras: OSRMProvider, GraphHopperProvider, GoogleMapsProvider.
 */
@Component
public class HaversineDistanceProvider implements DistanceProvider {

    @Override
    public String getProviderName() {
        return "HAVERSINE";
    }

    @Override
    public double distanceKm(GeoCoordinate from, GeoCoordinate to) {
        return from.distanceInKmTo(to);
    }

    @Override
    public double durationMinutes(GeoCoordinate from, GeoCoordinate to, double avgSpeedKmh) {
        double dist = distanceKm(from, to);
        return (dist / avgSpeedKmh) * 60.0;
    }
}
