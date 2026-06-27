package com.logicore.planning.infrastructure.pipeline;

import com.logicore.planning.domain.constraint.ConstraintViolation;
import com.logicore.planning.domain.distance.DistanceMatrix;
import com.logicore.planning.domain.pipeline.PipelineContext;
import com.logicore.planning.domain.pipeline.PipelineResult;
import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.PlanningOrder;
import com.logicore.planning.infrastructure.constraint.ConstraintEvaluatorService;
import com.logicore.planning.infrastructure.cost.CostFunctionService;
import com.logicore.planning.infrastructure.distance.HaversineDistanceProvider;
import com.logicore.planning.infrastructure.improvement.TwoOptImprover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Pipeline de otimização completo.
 *
 * Sequência de execução:
 *   1. Construir DistanceMatrix (pré-computada, evita recálculos)
 *   2. Executar estratégia → DecisionResult inicial
 *   3. Avaliar restrições → ConstraintViolation[]
 *   4. Aplicar 2-opt (se applyTwoOpt=true) → DecisionResult melhorado
 *   5. Calcular RouteScore com CostFunction
 *   6. Retornar PipelineResult com todos os dados
 */
@Component
public class OptimizationPipeline {

    private static final Logger log = LoggerFactory.getLogger(OptimizationPipeline.class);

    private final HaversineDistanceProvider distanceProvider;
    private final ConstraintEvaluatorService constraintEvaluator;
    private final TwoOptImprover twoOptImprover;
    private final CostFunctionService costFunction;

    public OptimizationPipeline(HaversineDistanceProvider distanceProvider,
                                 ConstraintEvaluatorService constraintEvaluator,
                                 TwoOptImprover twoOptImprover,
                                 CostFunctionService costFunction) {
        this.distanceProvider = distanceProvider;
        this.constraintEvaluator = constraintEvaluator;
        this.twoOptImprover = twoOptImprover;
        this.costFunction = costFunction;
    }

    public PipelineResult execute(PipelineContext ctx) {
        long start = System.currentTimeMillis();
        String strategyId = ctx.strategy().getIdentifier();

        log.debug("Pipeline iniciando: strategy={} orders={}", strategyId, ctx.orders().size());

        // 1. Pré-computar matriz de distâncias
        List<java.net.URI> locs = null; // unused
        var locations = ctx.orders().stream()
                .map(PlanningOrder::deliveryLocation)
                .collect(Collectors.toList());
        DistanceMatrix matrix = distanceProvider.buildMatrix(
                ctx.depot(), locations, ctx.constraints().averageSpeedKmh());

        // 2. Executar estratégia construtiva
        DecisionResult initial = ctx.strategy().execute(
                ctx.orders(), ctx.vehicle(), ctx.depot(), ctx.constraints(), ctx.strategyConfig());

        // 3. Avaliar restrições
        List<ConstraintViolation> violations = constraintEvaluator.evaluate(initial, ctx);

        // 4. Aplicar 2-opt (se solicitado e há ao menos 3 paradas)
        DecisionResult finalResult = initial;
        double twoOptImprovement = 0.0;
        boolean twoOptApplied = false;

        if (ctx.applyTwoOpt() && ctx.orders().size() >= 3) {
            var improved = twoOptImprover.improve(
                    initial, ctx.orders(), ctx.depot(), ctx.vehicle(), ctx.constraints(), matrix);
            if (improved.improvementKm() > 0.001) {
                finalResult = improved.result();
                twoOptImprovement = improved.improvementKm();
                twoOptApplied = true;
                log.debug("2-Opt aplicado: melhoria={:.2f}km strategy={}", twoOptImprovement, strategyId);
                // Re-avaliar após melhoria
                violations = constraintEvaluator.evaluate(finalResult, ctx);
            }
        }

        // 5. Calcular score composto
        var score = costFunction.calculate(finalResult, ctx.costWeights(), violations);

        long elapsed = System.currentTimeMillis() - start;
        log.debug("Pipeline concluído: strategy={} score={} time={}ms", strategyId, score.grade(), elapsed);

        return new PipelineResult(finalResult, score, violations, elapsed, twoOptApplied, twoOptImprovement);
    }
}
