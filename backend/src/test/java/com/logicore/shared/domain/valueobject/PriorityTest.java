package com.logicore.shared.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class PriorityTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    void validValuesAreAccepted(int value) {
        Priority p = new Priority(value);
        assertThat(p.value()).isEqualTo(value);
    }

    @Test
    void zeroThrows() {
        assertThatThrownBy(() -> new Priority(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sixThrows() {
        assertThatThrownBy(() -> new Priority(6))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isHigherThanReturnsTrueWhenHigher() {
        Priority high = new Priority(5);
        Priority low = new Priority(1);
        assertThat(high.isHigherThan(low)).isTrue();
    }

    @Test
    void isHigherThanReturnsFalseWhenLower() {
        Priority low = new Priority(1);
        Priority high = new Priority(5);
        assertThat(low.isHigherThan(high)).isFalse();
    }

    @Test
    void isHigherThanReturnsFalseWhenEqual() {
        Priority a = new Priority(3);
        Priority b = new Priority(3);
        assertThat(a.isHigherThan(b)).isFalse();
    }
}
