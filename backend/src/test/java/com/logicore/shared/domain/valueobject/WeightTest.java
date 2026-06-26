package com.logicore.shared.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class WeightTest {

    @Test
    void validWeightIsCreated() {
        Weight w = new Weight(100.0);
        assertThat(w.kilograms()).isEqualTo(100.0);
    }

    @Test
    void zeroWeightIsAllowed() {
        Weight w = new Weight(0.0);
        assertThat(w.kilograms()).isEqualTo(0.0);
    }

    @Test
    void negativeWeightThrows() {
        assertThatThrownBy(() -> new Weight(-0.1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addCombinesWeights() {
        Weight a = new Weight(50.0);
        Weight b = new Weight(30.0);
        assertThat(a.add(b).kilograms()).isEqualTo(80.0);
    }

    @Test
    void exceedsReturnsTrueWhenOverLimit() {
        Weight w = new Weight(100.1);
        Weight limit = new Weight(100.0);
        assertThat(w.exceeds(limit)).isTrue();
    }

    @Test
    void exceedsReturnsFalseWhenUnderLimit() {
        Weight w = new Weight(99.9);
        Weight limit = new Weight(100.0);
        assertThat(w.exceeds(limit)).isFalse();
    }

    @Test
    void exceedsReturnsFalseWhenEqual() {
        Weight w = new Weight(100.0);
        Weight limit = new Weight(100.0);
        assertThat(w.exceeds(limit)).isFalse();
    }
}
