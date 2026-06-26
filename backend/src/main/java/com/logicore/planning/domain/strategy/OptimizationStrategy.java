package com.logicore.planning.domain.strategy;

import com.logicore.core.domain.model.Vehicle;
import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.PlanningOrder;
import com.logicore.planning.domain.valueobject.RouteConstraints;
import com.logicore.shared.domain.valueobject.GeoCoordinate;

import java.util.List;

/**
 * Porta de saída plugável do Planning Engine.
 *
 * Contrato inviolável:
 * - NUNCA retorna null
 * - SEMPRE retorna DecisionResult com um DecisionStep por pedido
 * - NUNCA acessa repositório diretamente (domínio puro)
 * - NUNCA conhece HTTP, Spring ou JPA
 *
 * Implementações vivem na camada de infraestrutura (planning/infrastructure/strategy/).
 * Novas estratégias = nova classe implementando esta interface + registro no StrategyRegistry.
 */
public interface OptimizationStrategy {

    StrategyType getType();

    String getIdentifier();

    String getDescription();

    /**
     * Executa a estratégia de otimização.
     *
     * @param orders      PlanningOrders (order + customer data) — não vazio, já validados
     * @param vehicle     Veículo com capacidade disponível
     * @param depot       Ponto de partida da rota
     * @param constraints Restrições do contexto (velocidade, tempo de parada, etc.)
     * @param config      Configuração dinâmica específica desta estratégia
     * @return DecisionResult completo com steps explicados e métricas — nunca null
     */
    DecisionResult execute(
            List<PlanningOrder> orders,
            Vehicle vehicle,
            GeoCoordinate depot,
            RouteConstraints constraints,
            StrategyConfig config
    );
}
