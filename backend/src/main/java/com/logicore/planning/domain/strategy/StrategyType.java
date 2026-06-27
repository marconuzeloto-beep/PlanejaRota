package com.logicore.planning.domain.strategy;

public enum StrategyType {
    // Heurísticas construtivas
    SHORTEST_DISTANCE,
    FASTEST_TIME,
    PRIORITY_FIRST,
    HYBRID,
    CLARKE_WRIGHT_SAVINGS,
    A_STAR,

    // Algoritmos de melhoria local
    TWO_OPT,
    THREE_OPT,

    // Metaheurísticas
    SIMULATED_ANNEALING,
    TABU_SEARCH,
    GENETIC_ALGORITHM,
    ANT_COLONY_OPTIMIZATION
}
