package com.logicore.planning.infrastructure.strategy;

import com.logicore.planning.domain.strategy.OptimizationStrategy;
import com.logicore.planning.domain.strategy.StrategyType;
import com.logicore.shared.domain.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry de todas as OptimizationStrategy disponíveis no classpath.
 * Spring injeta automaticamente todas as implementações via List.
 * Novas estratégias = nova classe com @Component, nenhuma alteração aqui necessária.
 */
@Component
public class StrategyRegistry {

    private final Map<String, OptimizationStrategy> byIdentifier;
    private final Map<StrategyType, OptimizationStrategy> byType;

    public StrategyRegistry(List<OptimizationStrategy> strategies) {
        this.byIdentifier = strategies.stream()
                .collect(Collectors.toMap(OptimizationStrategy::getIdentifier, Function.identity()));
        this.byType = strategies.stream()
                .collect(Collectors.toMap(OptimizationStrategy::getType, Function.identity()));
    }

    @PostConstruct
    void log() {
        byIdentifier.forEach((id, s) ->
                System.out.printf("[StrategyRegistry] Loaded: %s → %s%n", id, s.getClass().getSimpleName()));
    }

    public OptimizationStrategy getByIdentifier(String identifier) {
        OptimizationStrategy s = byIdentifier.get(identifier);
        if (s == null) throw new ResourceNotFoundException("Estratégia não encontrada: " + identifier);
        return s;
    }

    public OptimizationStrategy getByType(StrategyType type) {
        OptimizationStrategy s = byType.get(type);
        if (s == null) throw new ResourceNotFoundException("Estratégia não encontrada para tipo: " + type);
        return s;
    }

    public List<OptimizationStrategy> listAll() {
        return List.copyOf(byIdentifier.values());
    }
}
