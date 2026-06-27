package com.logicore.planning.infrastructure.cost;

import com.logicore.planning.domain.constraint.ConstraintViolation;
import com.logicore.planning.domain.cost.CostWeights;
import com.logicore.planning.domain.cost.RouteScore;
import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.RouteMetrics;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Função de custo configurável.
 *
 * Score normalizado 0..1:
 *   score = Σ(weight_i × component_i)
 *
 * Cada componente é normalizado pelo valor de referência da rota.
 */
@Component
public class CostFunctionService {

    private static final double REFERENCE_DISTANCE_KM = 200.0;
    private static final int REFERENCE_TIME_MINUTES = 480;
    private static final double FUEL_CONSUMPTION_L_PER_100KM = 10.0;

    public RouteScore calculate(DecisionResult result, CostWeights weights,
                                 List<ConstraintViolation> violations) {
        RouteMetrics m = result.metrics();

        double distScore = normalize(m.totalDistanceKm(), REFERENCE_DISTANCE_KM);
        double timeScore = normalize(m.totalEstimatedTimeMinutes(), REFERENCE_TIME_MINUTES);
        double priorityScore = calcPriorityScore(result);
        double fuelScore = normalize(estimateFuelL(m.totalDistanceKm()), 20.0);
        double penaltyScore = penalties(violations);

        double total = weights.distanceWeight()  * distScore
                     + weights.timeWeight()       * timeScore
                     + weights.priorityWeight()   * priorityScore
                     + weights.fuelWeight()        * fuelScore
                     + weights.penaltyWeight()     * penaltyScore;

        return new RouteScore(
                clamp(total), clamp(distScore), clamp(timeScore),
                clamp(priorityScore), clamp(fuelScore), clamp(penaltyScore),
                weights, interpretation(total)
        );
    }

    private double normalize(double value, double reference) {
        return Math.max(0.0, 1.0 - (value / reference));
    }

    private double calcPriorityScore(DecisionResult result) {
        if (result.orderedSteps().isEmpty()) return 1.0;
        double sum = 0;
        int n = result.orderedSteps().size();
        for (var step : result.orderedSteps()) {
            double priorityWeight = step.priorityScore() / 5.0;
            double positionPenalty = (double) step.position() / n;
            sum += Math.max(0, priorityWeight - positionPenalty * 0.5);
        }
        return Math.min(1.0, sum / n * 2);
    }

    private double estimateFuelL(double distKm) {
        return (distKm * FUEL_CONSUMPTION_L_PER_100KM) / 100.0;
    }

    private double penalties(List<ConstraintViolation> violations) {
        if (violations.isEmpty()) return 1.0;
        long errors = violations.stream().filter(ConstraintViolation::isError).count();
        long warnings = violations.size() - errors;
        return Math.max(0.0, 1.0 - (errors * 0.15) - (warnings * 0.05));
    }

    private double clamp(double v) { return Math.max(0.0, Math.min(1.0, v)); }

    private String interpretation(double score) {
        if (score >= 0.85) return "Excelente — solução de alta qualidade";
        if (score >= 0.70) return "Boa — solução viável com margem de melhoria";
        if (score >= 0.55) return "Regular — restrições comprometendo eficiência";
        if (score >= 0.40) return "Fraca — considere algoritmo alternativo";
        return "Inadequada — revisar pedidos e restrições";
    }
}
