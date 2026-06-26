package com.planejarota.domain.valueobject;

import com.planejarota.domain.exception.InvalidCoordinateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class GeoCoordinateTest {

    @Test
    void shouldCreateValidCoordinate() {
        GeoCoordinate coord = new GeoCoordinate(-23.5505, -46.6333);
        assertThat(coord.latitude()).isEqualTo(-23.5505);
        assertThat(coord.longitude()).isEqualTo(-46.6333);
    }

    @ParameterizedTest
    @CsvSource({"-91, 0", "91, 0", "0, -181", "0, 181"})
    void shouldRejectInvalidCoordinates(double lat, double lon) {
        assertThatThrownBy(() -> new GeoCoordinate(lat, lon))
                .isInstanceOf(InvalidCoordinateException.class);
    }

    @Test
    void shouldCalculateHaversineDistance() {
        // São Paulo → Rio de Janeiro: ~357km
        GeoCoordinate saoPaulo = new GeoCoordinate(-23.5505, -46.6333);
        GeoCoordinate rio = new GeoCoordinate(-22.9068, -43.1729);

        double distance = saoPaulo.distanceInKmTo(rio);

        assertThat(distance).isBetween(350.0, 365.0);
    }

    @Test
    void shouldReturnZeroForSameCoordinate() {
        GeoCoordinate coord = new GeoCoordinate(-23.5505, -46.6333);
        assertThat(coord.distanceInKmTo(coord)).isEqualTo(0.0);
    }
}
