package com.logicore.planning.domain.cost;

/**
 * Score composto calculado pela função de custo.
 * Valores normalizados: 0.0 (pior) a 1.0 (melhor).
 */
public record RouteScore(
        double totalScore,
        double distanceScore,
        double timeScore,
        double priorityScore,
        double fuelScore,
        double penaltyScore,
        CostWeights weights,
        String interpretation
) {
    public static RouteScore perfect() {
        return new RouteScore(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, CostWeights.defaults(), "Solução perfeita");
    }

    public String grade() {
        if (totalScore >= 0.85) return "A";
        if (totalScore >= 0.70) return "B";
        if (totalScore >= 0.55) return "C";
        if (totalScore >= 0.40) return "D";
        return "F";
    }
}
