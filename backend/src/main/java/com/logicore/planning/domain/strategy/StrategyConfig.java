package com.logicore.planning.domain.strategy;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuração dinâmica da estratégia.
 * Cada estratégia documenta as chaves que aceita no campo weights/params.
 * Imutável após criação.
 */
public record StrategyConfig(
        Map<String, Double> weights,
        int maxIterations,
        Map<String, Object> params
) {
    public StrategyConfig {
        weights       = weights != null ? Map.copyOf(weights) : Map.of();
        params        = params != null ? Map.copyOf(params) : Map.of();
        maxIterations = maxIterations <= 0 ? 100 : maxIterations;
    }

    public static StrategyConfig defaults() {
        return new StrategyConfig(Map.of(), 100, Map.of());
    }

    public static StrategyConfig hybrid(double distanceWeight, double priorityWeight) {
        Map<String, Double> w = new HashMap<>();
        w.put("distance", distanceWeight);
        w.put("priority", priorityWeight);
        return new StrategyConfig(w, 100, Map.of());
    }

    public double weight(String key, double defaultValue) {
        return weights.getOrDefault(key, defaultValue);
    }
}
