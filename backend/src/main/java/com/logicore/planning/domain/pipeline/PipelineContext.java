package com.logicore.planning.domain.pipeline;

import com.logicore.core.domain.model.Vehicle;
import com.logicore.planning.domain.cost.CostWeights;
import com.logicore.planning.domain.strategy.OptimizationStrategy;
import com.logicore.planning.domain.strategy.StrategyConfig;
import com.logicore.planning.domain.valueobject.PlanningOrder;
import com.logicore.planning.domain.valueobject.RouteConstraints;
import com.logicore.shared.domain.valueobject.GeoCoordinate;

import java.util.List;
import java.util.UUID;

/**
 * Contexto imutável passado ao pipeline de otimização.
 * Contém todos os inputs necessários para uma execução completa.
 */
public record PipelineContext(
        UUID organizationId,
        List<PlanningOrder> orders,
        Vehicle vehicle,
        GeoCoordinate depot,
        RouteConstraints constraints,
        OptimizationStrategy strategy,
        StrategyConfig strategyConfig,
        CostWeights costWeights,
        boolean applyTwoOpt
) {
    public PipelineContext {
        orders = List.copyOf(orders);
        if (costWeights == null) costWeights = CostWeights.defaults();
    }

    public static PipelineContext of(UUID orgId, List<PlanningOrder> orders, Vehicle vehicle,
                                      GeoCoordinate depot, RouteConstraints constraints,
                                      OptimizationStrategy strategy, StrategyConfig config) {
        return new PipelineContext(orgId, orders, vehicle, depot, constraints,
                strategy, config, CostWeights.defaults(), true);
    }
}
