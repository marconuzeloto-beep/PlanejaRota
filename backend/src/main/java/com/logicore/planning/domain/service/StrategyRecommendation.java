package com.logicore.planning.domain.service;

import com.logicore.planning.domain.strategy.StrategyType;

import java.util.List;

public record StrategyRecommendation(
        String primaryIdentifier,
        StrategyType primaryType,
        String rationale,
        int estimatedTimeMs,
        double qualityRatio,
        List<AlternativeStrategy> alternatives
) {
    public record AlternativeStrategy(
            String identifier,
            StrategyType type,
            String tradeOff
    ) {}
}
