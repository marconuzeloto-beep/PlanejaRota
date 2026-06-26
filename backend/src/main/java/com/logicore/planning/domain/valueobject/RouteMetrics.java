package com.logicore.planning.domain.valueobject;

import java.math.BigDecimal;

/**
 * Métricas calculadas de uma solução de rota.
 * Produzidas pela estratégia de otimização — nunca calculadas fora do Planning Engine.
 */
public record RouteMetrics(
        double totalDistanceKm,
        int totalEstimatedTimeMinutes,
        BigDecimal estimatedCostBRL,
        double totalWeightKg,
        double capacityUsagePercent,
        int stopsWithTimeViolation,
        boolean feasible
) {
    public static RouteMetrics empty() {
        return new RouteMetrics(0, 0, BigDecimal.ZERO, 0, 0, 0, true);
    }
}
