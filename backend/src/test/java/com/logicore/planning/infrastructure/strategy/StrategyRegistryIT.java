package com.logicore.planning.infrastructure.strategy;

import com.logicore.planning.domain.strategy.StrategyType;
import com.logicore.shared.AbstractIntegrationTest;
import com.logicore.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

class StrategyRegistryIT extends AbstractIntegrationTest {

    @Autowired
    private StrategyRegistry strategyRegistry;

    @Test
    void allThreeStrategiesAreRegistered() {
        assertThat(strategyRegistry.listAll()).hasSize(3);
    }

    @Test
    void getByIdentifierShortestDistanceWorks() {
        var strategy = strategyRegistry.getByIdentifier("SHORTEST_DISTANCE_V1");
        assertThat(strategy).isNotNull();
        assertThat(strategy.getType()).isEqualTo(StrategyType.SHORTEST_DISTANCE);
    }

    @Test
    void getByTypeHybridWorks() {
        var strategy = strategyRegistry.getByType(StrategyType.HYBRID);
        assertThat(strategy).isNotNull();
        assertThat(strategy.getIdentifier()).isEqualTo("HYBRID_V1");
    }

    @Test
    void getByIdentifierNonExistentThrows() {
        assertThatThrownBy(() -> strategyRegistry.getByIdentifier("NON_EXISTENT"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
