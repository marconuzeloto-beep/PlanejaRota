package com.logicore.planning.domain.valueobject;

/**
 * Razão estruturada de uma decisão do algoritmo para uma parada específica.
 * O tipo é um enum para renderização frontend consistente.
 * O detalhe é texto human-readable gerado pelo algoritmo.
 */
public record DecisionReason(ReasonType type, String detail) {

    public enum ReasonType {
        NEAREST_NEIGHBOR,       // vizinho mais próximo disponível
        PRIORITY_OVERRIDE,      // prioridade alta forçou visita antes do mais próximo
        TIME_WINDOW_CONSTRAINT, // única posição que respeita a janela de tempo
        HYBRID_SCORE,           // score combinado de múltiplos critérios
        MANUAL_OVERRIDE,        // posição definida manualmente pelo usuário
        SAVINGS_ALGORITHM,      // fusão de rotas pelo algoritmo Clarke-Wright
        A_STAR_PATH,            // caminho escolhido via heurística A*
        TWO_OPT_IMPROVED,       // posição otimizada por 2-opt
        SIMULATED_ANNEALING,    // aceito por probabilidade de SA
        TABU_SEARCH,            // melhor vizinho não-tabu
        GENETIC_SELECTION,      // selecionado por crossover/mutação genética
        ACO_PHEROMONE           // guiado por trilha de feromônio ACO
    }

    public static DecisionReason nearestNeighbor(double distanceKm) {
        return new DecisionReason(ReasonType.NEAREST_NEIGHBOR,
                String.format("Vizinho mais próximo disponível: %.1f km", distanceKm));
    }

    public static DecisionReason priorityOverride(int priority, double distanceKm) {
        return new DecisionReason(ReasonType.PRIORITY_OVERRIDE,
                String.format("Prioridade %d — visitado antes do vizinho mais próximo (%.1f km)", priority, distanceKm));
    }

    public static DecisionReason hybridScore(double score, double distWeight, double prioWeight) {
        return new DecisionReason(ReasonType.HYBRID_SCORE,
                String.format("Score combinado %.2f (distância %.0f%% + prioridade %.0f%%)",
                        score, distWeight * 100, prioWeight * 100));
    }

    public static DecisionReason timeWindowConstraint(String window) {
        return new DecisionReason(ReasonType.TIME_WINDOW_CONSTRAINT,
                "Janela de tempo obrigatória: " + window);
    }

    public static DecisionReason savingsAlgorithm(double saving, double distFromPrev) {
        return new DecisionReason(ReasonType.SAVINGS_ALGORITHM,
                String.format("Clarke-Wright: economia de %.1f km, distância %.1f km", saving, distFromPrev));
    }

    public static DecisionReason aStarPath(double heuristic, double distFromPrev) {
        return new DecisionReason(ReasonType.A_STAR_PATH,
                String.format("A*: heurística %.1f km, distância %.1f km", heuristic, distFromPrev));
    }

    public static DecisionReason twoOptImproved(double improvement, double distFromPrev) {
        return new DecisionReason(ReasonType.TWO_OPT_IMPROVED,
                String.format("2-Opt: melhoria de %.1f km, distância %.1f km", improvement, distFromPrev));
    }

    public static DecisionReason simulatedAnnealing(double temperature, double distFromPrev) {
        return new DecisionReason(ReasonType.SIMULATED_ANNEALING,
                String.format("SA: temperatura %.2f, distância %.1f km", temperature, distFromPrev));
    }

    public static DecisionReason tabuSearch(int iteration, double distFromPrev) {
        return new DecisionReason(ReasonType.TABU_SEARCH,
                String.format("Tabu Search: iteração %d, distância %.1f km", iteration, distFromPrev));
    }

    public static DecisionReason geneticSelection(int generation, double fitness, double distFromPrev) {
        return new DecisionReason(ReasonType.GENETIC_SELECTION,
                String.format("GA: geração %d, fitness %.2f, distância %.1f km", generation, fitness, distFromPrev));
    }

    public static DecisionReason acoPheromone(double pheromone, double distFromPrev) {
        return new DecisionReason(ReasonType.ACO_PHEROMONE,
                String.format("ACO: feromônio %.3f, distância %.1f km", pheromone, distFromPrev));
    }
}
