# LogiCore — Planning Engine: Scientific Roadmap & Architecture

## 1. Arquitetura Definitiva do Planning Engine

### 1.1 Visão Geral

```
HTTP Request
    │
    ▼
RouteController
    │  PlanRouteRequest
    ▼
PlanRouteService                ← Application Layer
    │  PipelineContext
    ▼
OptimizationPipeline            ← Infrastructure Layer (orchestrator)
    │
    ├── 1. DistanceMatrix (HaversineDistanceProvider)
    │
    ├── 2. OptimizationStrategy.execute()   ← Pluggable Algorithm
    │       └── DecisionResult (initial)
    │
    ├── 3. ConstraintEvaluatorService
    │       ├── VehicleCapacityConstraint
    │       ├── TimeWindowConstraint
    │       ├── MaximumDistanceConstraint
    │       ├── MaximumWorkingHoursConstraint
    │       └── PriorityConstraint
    │       └── List<ConstraintViolation>
    │
    ├── 4. TwoOptImprover (se applyTwoOpt=true)
    │       └── DecisionResult (improved)
    │
    └── 5. CostFunctionService
            └── RouteScore (0.0–1.0, grade A–F)
    │
    ▼
PipelineResult
    ├── DecisionResult  (steps ordenados + métricas + explicações)
    ├── RouteScore      (score composto + breakdown)
    ├── List<ConstraintViolation>
    ├── executionTimeMs
    └── twoOptImprovementKm
```

### 1.2 Invariantes de Contrato

| Invariante | Descrição |
|---|---|
| `OptimizationStrategy` nunca retorna null | Garantido por `RouteBuilder` |
| Exatamente 1 `DecisionStep` por `PlanningOrder` | Validado em `RouteBuilder.build()` |
| `DecisionResult` é imutável | Java Records com `List.copyOf()` |
| Algoritmos não acessam repositórios | Estratégias são domínio puro |
| Pipeline sempre aplica 2-opt (padrão) | Configurável via `PipelineContext.applyTwoOpt` |

### 1.3 Extensibilidade

Para adicionar novo algoritmo:
1. Criar classe em `planning/infrastructure/strategy/`
2. Implementar `OptimizationStrategy`
3. Anotar com `@Component`
4. `StrategyRegistry` detecta automaticamente via Spring DI

**Zero modificações no núcleo do sistema.**

---

## 2. Biblioteca de Algoritmos

### 2.1 Heurísticas Construtivas

#### Nearest Neighbor (SHORTEST_DISTANCE_V1)
- **Ideia**: A cada passo, visita o cliente não-visitado mais próximo da posição atual.
- **Complexidade**: O(n²)
- **Qualidade**: 20–25% acima do ótimo em média
- **Caso de uso**: Baseline rápido, instâncias pequenas (n < 20)

#### Clarke-Wright Savings (CLARKE_WRIGHT_V1)
- **Ideia**: Calcula economias s(i,j) = d(0,i) + d(0,j) - d(i,j) e funde rotas por ordem decrescente.
- **Complexidade**: O(n² log n)
- **Qualidade**: ~10–15% acima do ótimo
- **Caso de uso**: VRP com múltiplos veículos, alta ocupação de capacidade

#### A* Heurístico (A_STAR_V1)
- **Ideia**: Adaptação do A* como construtivo: f(c) = g(c) + 0.5×h(c), onde h = distância ao próximo cliente mais próximo.
- **Complexidade**: O(n²)
- **Qualidade**: ~15–20% acima do ótimo
- **Diferencial sobre NN**: Olha para frente evitando clusters isolados

### 2.2 Algoritmos de Melhoria Local

#### 2-Opt (TWO_OPT_V1)
- **Ideia**: Testa todas as trocas de duas arestas. Se d(i,j) + d(i+1,j+1) < d(i,i+1) + d(j,j+1), reverte segmento.
- **Complexidade**: O(n² × k) onde k = número de iterações
- **Qualidade**: ~5% acima do ótimo (frequentemente ótimo local)
- **Caso de uso**: Refinamento pós-qualquer-construtivo. Aplicado automaticamente pelo pipeline.

### 2.3 Metaheurísticas

#### Simulated Annealing (SIMULATED_ANNEALING_V1)
- **Ideia**: Aceita soluções piores com probabilidade P = e^(-ΔE/T), T decrescendo.
- **Parâmetros chave**: T₀ = 1000, α = 0.995, T_min = 0.1
- **Complexidade**: O(n × maxIterations)
- **Qualidade**: 1–5% do ótimo com parâmetros adequados
- **Caso de uso**: Quando qualidade importa mais que tempo, n < 200

#### Tabu Search (TABU_SEARCH_V1)
- **Ideia**: Busca local com lista tabu de movimentos recentes. Critério de aspiração: aceita tabu se melhor que global best.
- **Parâmetros chave**: tenure = 10, maxIter = 500
- **Complexidade**: O(n² × maxIterations)
- **Qualidade**: 0.5–3% do ótimo
- **Caso de uso**: Instâncias médias (50–200), melhor qualidade que SA em TSP puro

#### Genetic Algorithm (GENETIC_ALGORITHM_V1)
- **Ideia**: Evolução de população de permutações. Operadores: Order Crossover (OX), swap mutation, tournament selection, elitismo.
- **Parâmetros chave**: popSize = max(50, 10n), gens = max(200, 20n), mutRate = 2%
- **Complexidade**: O(popSize × gens × n)
- **Qualidade**: 1–4% do ótimo, paralelizável
- **Caso de uso**: Instâncias grandes (n > 100), bom para multi-objetivo

#### Ant Colony Optimization (ANT_COLONY_V1)
- **Ideia**: Formigas constroem soluções guiadas por feromônio τ[i][j] e visibilidade η = 1/d[i][j].
  P(i→j) ∝ τ[i][j]^α × η[i][j]^β
- **Parâmetros chave**: nAnts = max(10,n), α=1, β=5, ρ=0.1, Q=100
- **Complexidade**: O(nAnts × n² × iterations)
- **Qualidade**: 1–3% do ótimo, excelente em problemas clustered
- **Caso de uso**: Problemas com estrutura de cluster natural (bairros, zonas)

---

## 3. Comparação Matemática

### 3.1 Razão de Aproximação (vs. Ótimo)

| Algoritmo | Razão típica | Garantia teórica |
|---|---|---|
| Nearest Neighbor | 1.20–1.25 | O(log n) - sem garantia strong |
| Clarke-Wright | 1.10–1.15 | Sem garantia formal |
| A* Heurístico | 1.15–1.20 | Sem garantia formal |
| 2-Opt | 1.02–1.10 | Sem garantia (ótimo local) |
| Simulated Annealing | 1.01–1.05 | Ótimo com prob→1 (resfr. lento) |
| Tabu Search | 1.01–1.03 | Sem garantia formal |
| Genetic Algorithm | 1.01–1.04 | Sem garantia formal |
| Ant Colony | 1.01–1.03 | Sem garantia formal |

*Nota: TSP é NP-difícil. Christofides garante 3/2-aproximação mas não implementado.*

### 3.2 Escalabilidade Empírica (estimado, CPU single-thread)

| n (pedidos) | NN | CW | 2-Opt | SA | TS | GA | ACO |
|---|---|---|---|---|---|---|---|
| 10 | < 1ms | < 1ms | < 1ms | 5ms | 10ms | 20ms | 30ms |
| 50 | 1ms | 2ms | 10ms | 50ms | 200ms | 500ms | 300ms |
| 100 | 5ms | 15ms | 100ms | 200ms | 2s | 3s | 2s |
| 500 | 50ms | 200ms | 5s | 1s | 50s | 60s | 40s |
| 1000 | 200ms | 800ms | 30s | 5s | >2min | >5min | >3min |

**Recomendações por n**:
- n ≤ 20: Qualquer algoritmo. Preferir SA ou TS pela qualidade.
- 20 < n ≤ 100: Clarke-Wright + 2-Opt. SA se tempo disponível.
- 100 < n ≤ 500: GA ou ACO com 2-Opt. NN+2-Opt como fallback rápido.
- n > 500: NN+2-Opt (qualidade razoável, tempo controlado). GA com população reduzida.

---

## 4. Função de Custo (RouteScore)

```
Score = 0.40 × distanceScore
      + 0.25 × timeScore
      + 0.20 × priorityScore
      + 0.10 × fuelScore
      + 0.05 × penaltyScore
```

Todos os componentes normalizados [0, 1] contra valores de referência.

**Graus**:
- A (≥ 85%): Solução de alta qualidade
- B (≥ 70%): Viável com margem de melhoria
- C (≥ 55%): Restrições comprometendo eficiência
- D (≥ 40%): Considerar algoritmo alternativo
- F (< 40%): Revisar pedidos e restrições

---

## 5. Sistema de Restrições

Cada restrição é um `@Component` Spring implementando `RouteConstraint`.
O `ConstraintEvaluatorService` agrega todos via injeção de lista.

| Restrição | Severidade | Gatilho |
|---|---|---|
| VehicleCapacityConstraint | ERROR | peso total > capacidade |
| TimeWindowConstraint | ERROR | chegada fora da janela (hardTimeWindows=true) |
| MaximumDistanceConstraint | WARNING | distância total > 500 km |
| MaximumWorkingHoursConstraint | WARNING | tempo estimado > 10h |
| PriorityConstraint | WARNING | cliente P≥4 visitado após 60% da rota |

---

## 6. Provedor de Distâncias

```java
interface DistanceProvider {
    double distanceKm(GeoCoordinate from, GeoCoordinate to);
    double durationMinutes(GeoCoordinate from, GeoCoordinate to, double avgSpeedKmh);
    DistanceMatrix buildMatrix(GeoCoordinate depot, List<GeoCoordinate> locations, double avgSpeedKmh);
}
```

**Implementações planejadas**:

| Provedor | Status | Precisão | Uso |
|---|---|---|---|
| HaversineDistanceProvider | ✅ Ativo | < 0.5% erro | Desenvolvimento e POC |
| OSRMProvider | Planejado | Real (ruas) | Produção com OSRM self-hosted |
| GraphHopperProvider | Planejado | Real (ruas) | Produção com GraphHopper |
| GoogleMapsProvider | Planejado | Real + tráfego | Produção premium |

A troca de provedor requer apenas alterar o `@Primary` bean — nenhum algoritmo muda.

---

## 7. Respostas às Perguntas Finais

### 7.1 Qual algoritmo é o padrão do sistema?

**Nearest Neighbor (SHORTEST_DISTANCE_V1)** é o algoritmo construtivo padrão, sempre executado com **2-Opt automático** via pipeline.

Justificativa:
- Simples, determinístico, rápido
- Resultado previsível e explicável
- 2-Opt pós-execução compensa a qualidade inferior

### 7.2 Qual produz a melhor solução?

**Tabu Search (TABU_SEARCH_V1)** e **Simulated Annealing (SIMULATED_ANNEALING_V1)** consistentemente produzem as melhores soluções em instâncias TSP clássicas.

Para instâncias com clusters naturais: **ACO (ANT_COLONY_V1)**.
Para instâncias grandes (n > 200): **Genetic Algorithm (GENETIC_ALGORITHM_V1)** (paralelizável).

### 7.3 Qual é o mais rápido?

**Nearest Neighbor (SHORTEST_DISTANCE_V1)**: O(n²), microsegundos para n < 100.

Se precisar de velocidade com qualidade: **Clarke-Wright** é 10x mais lento que NN mas produz soluções ~15% melhores.

### 7.4 Qual escala melhor?

**Simulated Annealing (SIMULATED_ANNEALING_V1)**: O(n × iter), onde iter é configurável. Pode escalar para n=10.000 com iter reduzido. Tempo controlado, qualidade razoável.

**Nearest Neighbor** também escala bem mas com qualidade declinante para n > 50.

### 7.5 Qual deve ser utilizado em produção?

**Recomendação de produção**:

```
n ≤ 30:   TABU_SEARCH_V1   (melhor qualidade, tempo aceitável)
30 < n ≤ 100: SIMULATED_ANNEALING_V1 + 2-Opt (pipeline automático)
n > 100:  CLARKE_WRIGHT_V1 + 2-Opt  (melhor trade-off qualidade/tempo)
```

Sempre com **2-Opt** automático via `OptimizationPipeline`.

Para SLA < 500ms: **SHORTEST_DISTANCE_V1** (NN) é sempre seguro.

### 7.6 Como adicionar novos algoritmos sem modificar o núcleo?

```java
// 1. Criar a implementação
@Component
public class MyNewStrategy implements OptimizationStrategy {
    @Override public String getIdentifier() { return "MY_STRATEGY_V1"; }
    @Override public StrategyType getType() { return StrategyType.MY_TYPE; }
    @Override public String getDescription() { return "Minha estratégia"; }
    
    @Override
    public DecisionResult execute(List<PlanningOrder> orders, Vehicle vehicle,
                                   GeoCoordinate depot, RouteConstraints constraints,
                                   StrategyConfig config) {
        // ... implementar algoritmo ...
        return DecisionResult.of(getType(), config, steps, metrics);
    }
}

// 2. Adicionar ao StrategyType enum (única alteração no domínio)
// 3. StrategyRegistry detecta automaticamente via Spring DI
// 4. Disponível em /api/v1/routes/plan e /api/v1/benchmark/run imediatamente
```

**Nenhuma modificação nos controladores, serviços de aplicação, pipeline ou banco de dados.**

---

## 8. Roadmap de Pesquisa Futura

### Curto prazo (1–3 meses)
- [ ] **3-Opt**: Melhoria local mais agressiva que 2-opt. O(n³) por iter.
- [ ] **Or-Opt**: Move 1, 2 ou 3 clientes para outras posições. Mais rápido que 3-opt.
- [ ] **OSRMProvider**: Integrar OSRM self-hosted para distâncias reais de rua
- [ ] **DistanceMatrixCache**: Redis cache de matrizes por conjunto de coordenadas

### Médio prazo (3–6 meses)
- [ ] **LKH-3 (Lin-Kernighan-Helsgott)**: Estado da arte para TSP puro. Quase-ótimo.
- [ ] **Multi-objetivo**: Frente de Pareto (distância × tempo × custo × prioridade)
- [ ] **VRPTW Exato**: Branch-and-bound para instâncias pequenas (n ≤ 20)
- [ ] **Paralelização**: GA e ACO naturalmente paralelizáveis com Virtual Threads (Java 21)

### Longo prazo (6+ meses)
- [ ] **ML-guided heuristics**: Treinar classificador de "melhor algoritmo por instância"
- [ ] **Graph Neural Network** para predição de custo de tour (DL4J ou ONNX)
- [ ] **Reinforcement Learning** (Policy Gradient) para construção de rota
- [ ] **CVRP com janelas de tempo múltiplas** (CVRPTW)
- [ ] **Multi-depot VRP**: Múltiplos depósitos com atribuição automática

---

## 9. Referências Científicas

1. Clarke, G. and Wright, J.W. (1964). Scheduling of vehicles from a central depot to a number of delivery points. *Operations Research*, 12(4), 568-581.
2. Lin, S. (1965). Computer solutions of the traveling salesman problem. *Bell System Technical Journal*, 44(10), 2245-2269.
3. Kirkpatrick, S., Gelatt, C.D., Holland, J.H. (1983). Optimization by simulated annealing. *Science*, 220(4598), 671-680.
4. Glover, F. (1989). Tabu search—Part I. *ORSA Journal on Computing*, 1(3), 190-206.
5. Dorigo, M., Gambardella, L.M. (1997). Ant colony system: A cooperative learning approach to the traveling salesman problem. *IEEE Transactions on Evolutionary Computation*, 1(1), 53-66.
6. Goldberg, D.E. (1989). *Genetic Algorithms in Search, Optimization and Machine Learning*. Addison-Wesley.
7. Applegate, D., Bixby, R., Chvátal, V., Cook, W. (2006). *The Traveling Salesman Problem: A Computational Study*. Princeton University Press.
