package com.logicore.shared.domain.valueobject;

import com.logicore.shared.domain.exception.InvalidCoordinateException;

/**
 * Representa um ponto geográfico imutável (latitude, longitude).
 * Encapsula validação e cálculo de distância Haversine.
 */
public record GeoCoordinate(double latitude, double longitude) {

    private static final double EARTH_RADIUS_KM = 6371.0;

    public GeoCoordinate {
        if (latitude < -90 || latitude > 90)
            throw new InvalidCoordinateException("Latitude inválida: " + latitude + " (deve estar em [-90, 90])");
        if (longitude < -180 || longitude > 180)
            throw new InvalidCoordinateException("Longitude inválida: " + longitude + " (deve estar em [-180, 180])");
    }

    /**
     * Distância em km usando fórmula de Haversine.
     * Erro < 0.5% em distâncias urbanas — suficiente para planejamento logístico.
     */
    public double distanceInKmTo(GeoCoordinate other) {
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(this.latitude))
                * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
