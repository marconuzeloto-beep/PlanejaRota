package com.logicore.planning.infrastructure.distance;

import com.logicore.planning.domain.distance.DistanceProvider;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import org.springframework.stereotype.Component;

@Component("euclideanDistanceProvider")
public class EuclideanDistanceProvider implements DistanceProvider {

    private static final double EARTH_RADIUS_KM = 6371.0;

    @Override
    public String getProviderName() {
        return "EUCLIDEAN";
    }

    @Override
    public double distanceKm(GeoCoordinate from, GeoCoordinate to) {
        double latDiff = Math.toRadians(to.latitude() - from.latitude()) * EARTH_RADIUS_KM;
        double lngDiff = Math.toRadians(to.longitude() - from.longitude())
                * EARTH_RADIUS_KM * Math.cos(Math.toRadians((from.latitude() + to.latitude()) / 2));
        return Math.sqrt(latDiff * latDiff + lngDiff * lngDiff);
    }

    @Override
    public double durationMinutes(GeoCoordinate from, GeoCoordinate to, double avgSpeedKmh) {
        return (distanceKm(from, to) / avgSpeedKmh) * 60.0;
    }
}
