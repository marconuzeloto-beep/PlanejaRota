package com.logicore.planning.domain.strategy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class StrategyConfigTest {

    @Test
    void defaultsHasReasonableValues() {
        StrategyConfig config = StrategyConfig.defaults();
        assertThat(config.maxIterations()).isEqualTo(100);
        assertThat(config.weights()).isEmpty();
        assertThat(config.params()).isEmpty();
    }

    @Test
    void weightReturnsKeyValueWhenPresent() {
        StrategyConfig config = StrategyConfig.hybrid(0.7, 0.3);
        assertThat(config.weight("distance", 0.5)).isEqualTo(0.7);
        assertThat(config.weight("priority", 0.5)).isEqualTo(0.3);
    }

    @Test
    void weightReturnsDefaultWhenKeyAbsent() {
        StrategyConfig config = StrategyConfig.defaults();
        assertThat(config.weight("nonexistent", 0.42)).isEqualTo(0.42);
    }

    @Test
    void hybridFactorySetsWeights() {
        StrategyConfig config = StrategyConfig.hybrid(0.6, 0.4);
        assertThat(config.weight("distance", 0.0)).isEqualTo(0.6);
        assertThat(config.weight("priority", 0.0)).isEqualTo(0.4);
        assertThat(config.maxIterations()).isEqualTo(100);
    }

    @Test
    void zeroOrNegativeMaxIterationsDefaultsTo100() {
        StrategyConfig config = new StrategyConfig(null, 0, null);
        assertThat(config.maxIterations()).isEqualTo(100);

        StrategyConfig config2 = new StrategyConfig(null, -5, null);
        assertThat(config2.maxIterations()).isEqualTo(100);
    }
}
