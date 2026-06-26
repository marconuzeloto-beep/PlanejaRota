package com.logicore.planning.domain.service;

import com.logicore.core.domain.model.Vehicle;
import com.logicore.core.domain.model.VehicleStatus;
import com.logicore.planning.domain.strategy.OptimizationStrategy;
import com.logicore.planning.domain.strategy.StrategyConfig;
import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.PlanningOrder;
import com.logicore.planning.domain.valueobject.RouteConstraints;
import com.logicore.shared.domain.exception.BusinessRuleException;
import com.logicore.shared.domain.valueobject.GeoCoordinate;

import java.util.List;

/**
 * Serviço de domínio do Planning Engine.
 * Valida pré-condições e delega ao OptimizationStrategy.
 * Não acessa repositórios — puro domínio.
 */
public class RouteBuilder {

    private RouteBuilder() {}

    /**
     * Executa a estratégia com validação prévia de constraints.
     *
     * @return DecisionResult completo — nunca null
     * @throws BusinessRuleException se os inputs violam constraints do domínio
     */
    public static DecisionResult build(
            List<PlanningOrder> orders,
            Vehicle vehicle,
            GeoCoordinate depot,
            RouteConstraints constraints,
            OptimizationStrategy strategy,
            StrategyConfig config
    ) {
        validateInputs(orders, vehicle, depot, constraints, strategy);

        double totalWeightKg = orders.stream()
                .mapToDouble(o -> o.weight().kilograms())
                .sum();

        if (totalWeightKg > constraints.maxWeightKg()) {
            throw new BusinessRuleException(String.format(
                    "Peso total dos pedidos (%.1f kg) excede capacidade do veículo (%.1f kg)",
                    totalWeightKg, constraints.maxWeightKg()));
        }

        DecisionResult result = strategy.execute(orders, vehicle, depot, constraints, config);

        if (result == null) {
            throw new IllegalStateException("Estratégia " + strategy.getIdentifier() + " retornou null — violação de contrato");
        }
        if (result.orderedSteps().size() != orders.size()) {
            throw new IllegalStateException(String.format(
                    "Estratégia retornou %d steps para %d pedidos — violação de contrato",
                    result.orderedSteps().size(), orders.size()));
        }

        return result;
    }

    private static void validateInputs(List<PlanningOrder> orders, Vehicle vehicle,
                                        GeoCoordinate depot, RouteConstraints constraints,
                                        OptimizationStrategy strategy) {
        if (orders == null || orders.isEmpty()) {
            throw new BusinessRuleException("Pedidos são obrigatórios para construir uma rota");
        }
        if (vehicle == null) {
            throw new BusinessRuleException("Veículo é obrigatório");
        }
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new BusinessRuleException("Veículo deve estar AVAILABLE para planejar rota");
        }
        if (depot == null) {
            throw new BusinessRuleException("Ponto de partida (depot) é obrigatório");
        }
        if (constraints == null) {
            throw new BusinessRuleException("Constraints são obrigatórias");
        }
        if (strategy == null) {
            throw new BusinessRuleException("Estratégia de otimização é obrigatória");
        }
    }
}
