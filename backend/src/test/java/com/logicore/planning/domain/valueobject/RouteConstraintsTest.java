package com.logicore.planning.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class RouteConstraintsTest {

    @Test
    void withDefaultsCreatesValidObject() {
        RouteConstraints constraints = RouteConstraints.withDefaults(1000.0);
        assertThat(constraints.maxWeightKg()).isEqualTo(1000.0);
        assertThat(constraints.averageSpeedKmh()).isEqualTo(40.0);
        assertThat(constraints.stopDurationMinutes()).isEqualTo(10);
        assertThat(constraints.hardTimeWindows()).isFalse();
    }

    @Test
    void invalidSpeedThrows() {
        assertThatThrownBy(() -> new RouteConstraints(1000.0, 0.0, 10, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RouteConstraints(1000.0, -1.0, 10, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void invalidWeightThrows() {
        assertThatThrownBy(() -> new RouteConstraints(0.0, 40.0, 10, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RouteConstraints(-1.0, 40.0, 10, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativeStopDurationThrows() {
        assertThatThrownBy(() -> new RouteConstraints(1000.0, 40.0, -1, false))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
