package com.logicore.planning.domain.service;

import com.logicore.planning.domain.strategy.StrategyType;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Domain service that selects the optimal optimization strategy based on
 * problem characteristics: order count, time windows, SLA constraint, and
 * cargo complexity. This is pure domain logic with no infrastructure dependency.
 */
@Service
public class StrategyAdvisor {

    public StrategyRecommendation recommend(AdvisorRequest request) {
        int n = request.orderCount();
        boolean hasTimeWindows = request.hasTimeWindows();
        boolean hasHazardousCargo = request.hasHazardousCargo();
        long slaMsLimit = request.slaMsLimit();

        // Hard SLA — must stay under time budget
        if (slaMsLimit > 0 && slaMsLimit < 200) {
            return fastSolution(n);
        }

        if (n <= 10) {
            return recommend_tiny(hasTimeWindows, slaMsLimit);
        } else if (n <= 30) {
            return recommend_small(hasTimeWindows, slaMsLimit);
        } else if (n <= 100) {
            return recommend_medium(hasTimeWindows, hasHazardousCargo, slaMsLimit);
        } else {
            return recommend_large(n, slaMsLimit);
        }
    }

    private StrategyRecommendation fastSolution(int n) {
        return new StrategyRecommendation(
                "SHORTEST_DISTANCE_V1", StrategyType.SHORTEST_DISTANCE,
                "SLA < 200ms exige Nearest Neighbor O(n²) — garantido sub-milissegundo para n < 200.",
                estimateMs("SHORTEST_DISTANCE_V1", n), 1.22,
                List.of(
                        new StrategyRecommendation.AlternativeStrategy(
                                "CLARKE_WRIGHT_V1", StrategyType.CLARKE_WRIGHT_SAVINGS,
                                "10% melhor qualidade, mas 3-5x mais lento")
                )
        );
    }

    private StrategyRecommendation recommend_tiny(boolean hasTimeWindows, long slaMsLimit) {
        if (hasTimeWindows) {
            return new StrategyRecommendation(
                    "TABU_SEARCH_V1", StrategyType.TABU_SEARCH,
                    "n ≤ 10 com janelas de tempo: Tabu Search explora completamente o espaço de soluções viáveis.",
                    estimateMs("TABU_SEARCH_V1", 10), 1.01,
                    List.of(
                            new StrategyRecommendation.AlternativeStrategy(
                                    "SIMULATED_ANNEALING_V1", StrategyType.SIMULATED_ANNEALING,
                                    "Qualidade similar, mais rápido para instâncias pequenas")
                    )
            );
        }
        return new StrategyRecommendation(
                "TABU_SEARCH_V1", StrategyType.TABU_SEARCH,
                "n ≤ 10: Tabu Search atinge ótimo ou próximo em microssegundos.",
                estimateMs("TABU_SEARCH_V1", 10), 1.01,
                List.of(
                        new StrategyRecommendation.AlternativeStrategy(
                                "SIMULATED_ANNEALING_V1", StrategyType.SIMULATED_ANNEALING,
                                "Equivalente em qualidade, performance estocástica")
                )
        );
    }

    private StrategyRecommendation recommend_small(boolean hasTimeWindows, long slaMsLimit) {
        boolean timeSensitive = slaMsLimit > 0 && slaMsLimit < 1000;
        if (timeSensitive) {
            return new StrategyRecommendation(
                    "CLARKE_WRIGHT_V1", StrategyType.CLARKE_WRIGHT_SAVINGS,
                    "n ≤ 30 com SLA < 1s: Clarke-Wright oferece 10-15% acima do ótimo em < 5ms.",
                    estimateMs("CLARKE_WRIGHT_V1", 30), 1.12,
                    List.of(
                            new StrategyRecommendation.AlternativeStrategy(
                                    "TABU_SEARCH_V1", StrategyType.TABU_SEARCH,
                                    "Melhor qualidade mas pode exceder SLA")
                    )
            );
        }
        return new StrategyRecommendation(
                "SIMULATED_ANNEALING_V1", StrategyType.SIMULATED_ANNEALING,
                "n ≤ 30: SA explora o espaço de busca com aceitação de pioras controlada — 1-5% do ótimo.",
                estimateMs("SIMULATED_ANNEALING_V1", 30), 1.03,
                List.of(
                        new StrategyRecommendation.AlternativeStrategy(
                                "TABU_SEARCH_V1", StrategyType.TABU_SEARCH,
                                "Melhor em TSP puro, mesma faixa de qualidade"),
                        new StrategyRecommendation.AlternativeStrategy(
                                "CLARKE_WRIGHT_V1", StrategyType.CLARKE_WRIGHT_SAVINGS,
                                "5x mais rápido, 12% acima do ótimo")
                )
        );
    }

    private StrategyRecommendation recommend_medium(boolean hasTimeWindows, boolean hasHazardous, long slaMsLimit) {
        if (hasHazardous) {
            // Hazardous cargo requires strict feasibility — SA with more conservative moves
            return new StrategyRecommendation(
                    "SIMULATED_ANNEALING_V1", StrategyType.SIMULATED_ANNEALING,
                    "Carga perigosa: SA com temperatura conservadora garante viabilidade máxima das restrições.",
                    estimateMs("SIMULATED_ANNEALING_V1", 75), 1.04,
                    List.of(
                            new StrategyRecommendation.AlternativeStrategy(
                                    "TABU_SEARCH_V1", StrategyType.TABU_SEARCH,
                                    "Alternativa robusta com lista tabu evitando movimentos infeasíveis")
                    )
            );
        }
        if (slaMsLimit > 0 && slaMsLimit < 500) {
            return new StrategyRecommendation(
                    "CLARKE_WRIGHT_V1", StrategyType.CLARKE_WRIGHT_SAVINGS,
                    "30 < n ≤ 100 com SLA < 500ms: Clarke-Wright + 2-Opt automático, solução em < 200ms.",
                    estimateMs("CLARKE_WRIGHT_V1", 75), 1.10,
                    List.of(
                            new StrategyRecommendation.AlternativeStrategy(
                                    "SHORTEST_DISTANCE_V1", StrategyType.SHORTEST_DISTANCE,
                                    "Mais rápido ainda, 20-25% acima do ótimo")
                    )
            );
        }
        return new StrategyRecommendation(
                "ANT_COLONY_V1", StrategyType.ANT_COLONY_OPTIMIZATION,
                "30 < n ≤ 100: ACO com feromônio explora clusters naturais — ideal para rotas urbanas zonadas.",
                estimateMs("ANT_COLONY_V1", 75), 1.02,
                List.of(
                        new StrategyRecommendation.AlternativeStrategy(
                                "GENETIC_ALGORITHM_V1", StrategyType.GENETIC_ALGORITHM,
                                "Paralelizável, melhor para instâncias sem estrutura de cluster"),
                        new StrategyRecommendation.AlternativeStrategy(
                                "SIMULATED_ANNEALING_V1", StrategyType.SIMULATED_ANNEALING,
                                "Mais rápido, qualidade próxima")
                )
        );
    }

    private StrategyRecommendation recommend_large(int n, long slaMsLimit) {
        if (slaMsLimit > 0 && slaMsLimit < 2000) {
            return new StrategyRecommendation(
                    "SHORTEST_DISTANCE_V1", StrategyType.SHORTEST_DISTANCE,
                    "n > 100 com SLA agressivo: Nearest Neighbor + 2-Opt automático — resultado razoável em < 1s.",
                    estimateMs("SHORTEST_DISTANCE_V1", n), 1.22,
                    List.of(
                            new StrategyRecommendation.AlternativeStrategy(
                                    "CLARKE_WRIGHT_V1", StrategyType.CLARKE_WRIGHT_SAVINGS,
                                    "10% melhor qualidade, pode exceder SLA para n > 200")
                    )
            );
        }
        return new StrategyRecommendation(
                "GENETIC_ALGORITHM_V1", StrategyType.GENETIC_ALGORITHM,
                "n > 100: GA com população proporcional a n é naturalmente paralelizável — melhor trade-off qualidade/tempo.",
                estimateMs("GENETIC_ALGORITHM_V1", n), 1.03,
                List.of(
                        new StrategyRecommendation.AlternativeStrategy(
                                "ANT_COLONY_V1", StrategyType.ANT_COLONY_OPTIMIZATION,
                                "Excelente para problemas geográficos clustered"),
                        new StrategyRecommendation.AlternativeStrategy(
                                "SIMULATED_ANNEALING_V1", StrategyType.SIMULATED_ANNEALING,
                                "O(n×iter) controlável — ideal quando tempo é crítico")
                )
        );
    }

    private static int estimateMs(String identifier, int n) {
        return switch (identifier) {
            case "SHORTEST_DISTANCE_V1" -> Math.max(1, n * n / 1000);
            case "CLARKE_WRIGHT_V1"     -> Math.max(2, n * n / 200);
            case "A_STAR_V1"            -> Math.max(2, n * n / 500);
            case "SIMULATED_ANNEALING_V1" -> Math.max(5, n * 2);
            case "TABU_SEARCH_V1"       -> Math.max(10, n * n / 50);
            case "GENETIC_ALGORITHM_V1" -> Math.max(20, n * 30);
            case "ANT_COLONY_V1"        -> Math.max(30, n * 20);
            default                     -> 50;
        };
    }
}
