package com.logicore.planning.domain.cost;

/**
 * Pesos configuráveis da função de custo por organização.
 * score = w_dist*distance + w_time*time + w_prio*priority + w_fuel*fuel + w_penalty*penalties
 */
public record CostWeights(
        double distanceWeight,
        double timeWeight,
        double priorityWeight,
        double fuelWeight,
        double penaltyWeight
) {
    public CostWeights {
        double sum = distanceWeight + timeWeight + priorityWeight + fuelWeight + penaltyWeight;
        if (Math.abs(sum - 1.0) > 0.001)
            throw new IllegalArgumentException(
                String.format("Pesos devem somar 1.0, soma atual: %.3f", sum));
    }

    public static CostWeights defaults() {
        return new CostWeights(0.40, 0.25, 0.20, 0.10, 0.05);
    }

    public static CostWeights distanceOnly() {
        return new CostWeights(1.0, 0.0, 0.0, 0.0, 0.0);
    }

    public static CostWeights priorityFirst() {
        return new CostWeights(0.15, 0.15, 0.50, 0.10, 0.10);
    }
}
