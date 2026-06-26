package com.logicore.shared.domain.valueobject;

import com.logicore.shared.domain.exception.InvalidCoordinateException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class GeoCoordinateTest {

    @Test
    void validCoordinateIsCreated() {
        GeoCoordinate coord = new GeoCoordinate(-23.5505, -46.6333);
        assertThat(coord.latitude()).isEqualTo(-23.5505);
        assertThat(coord.longitude()).isEqualTo(-46.6333);
    }

    @Test
    void invalidLatitudeThrows() {
        assertThatThrownBy(() -> new GeoCoordinate(91.0, 0.0))
                .isInstanceOf(InvalidCoordinateException.class);
        assertThatThrownBy(() -> new GeoCoordinate(-91.0, 0.0))
                .isInstanceOf(InvalidCoordinateException.class);
    }

    @Test
    void invalidLongitudeThrows() {
        assertThatThrownBy(() -> new GeoCoordinate(0.0, 181.0))
                .isInstanceOf(InvalidCoordinateException.class);
        assertThatThrownBy(() -> new GeoCoordinate(0.0, -181.0))
                .isInstanceOf(InvalidCoordinateException.class);
    }

    @Test
    void distanceSaoPauloToRioIsApproximately360km() {
        // São Paulo
        GeoCoordinate saoPaulo = new GeoCoordinate(-23.5505, -46.6333);
        // Rio de Janeiro
        GeoCoordinate rio = new GeoCoordinate(-22.9068, -43.1729);

        double distance = saoPaulo.distanceInKmTo(rio);

        // ~360 km, within 10%
        assertThat(distance).isBetween(324.0, 396.0);
    }

    @Test
    void distanceToSelfIsZero() {
        GeoCoordinate coord = new GeoCoordinate(-23.5505, -46.6333);
        assertThat(coord.distanceInKmTo(coord)).isLessThan(0.001);
    }
}
