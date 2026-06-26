package com.planejarota.domain.valueobject;

import com.planejarota.domain.exception.InvalidCoordinateException;

/**
 * Representa um ponto geográfico imutável.
 *
 * Encapsula a lógica de validação e operações geoespaciais básicas,
 * evitando que coordenadas inválidas trafeguem pelo domínio.
 */
public record GeoCoordinate(double latitude, double longitude) {

    private static final double EARTH_RADIUS_KM = 6371.0;

    public GeoCoordinate {
        if (latitude < -90 || latitude > 90) {
            throw new InvalidCoordinateException("Latitude deve estar entre -90 e 90, recebido: " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new InvalidCoordinateException("Longitude deve estar entre -180 e 180, recebido: " + longitude);
        }
    }

    /**
     * Calcula distância em quilômetros usando a fórmula de Haversine.
     * Precisão suficiente para planejamento logístico (erro < 0.5% em distâncias urbanas).
     */
    public double distanceInKmTo(GeoCoordinate other) {
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(this.latitude))
                * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    @Override
    public String toString() {
        return String.format("(%f, %f)", latitude, longitude);
    }
}
