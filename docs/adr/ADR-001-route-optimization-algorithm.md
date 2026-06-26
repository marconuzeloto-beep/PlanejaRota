# ADR-001: Algoritmo de Otimização de Rotas

**Status:** Aceito  
**Data:** 2026-06-26

## Contexto

O núcleo do PlanejaRota é a otimização de rotas de entrega. O problema é uma variação do VRP (Vehicle Routing Problem), que é NP-difícil. Precisamos decidir entre:

1. Implementação própria de heurística
2. Integração com OSRM (Open Source Routing Machine)
3. Integração com serviço externo pago (Google Maps Directions API)

## Decisão

**Fase 1:** Implementação própria com heurística Nearest Neighbor + 2-opt local search.

**Fase 2 (futura):** Integração com OSRM para distâncias reais de rua (não distância euclidiana).

## Justificativa

### Por que não Google Maps API agora?
- Custo por requisição inviabiliza MVP e testes
- Vendor lock-in
- Dados não ficam no nosso controle

### Por que não OSRM agora?
- Requer infraestrutura adicional (servidor com dados do OSM ~64GB para Brasil)
- Complexidade de operação incompatível com fase inicial

### Por que Nearest Neighbor + 2-opt?
- Implementação controlada, sem dependências externas
- Nearest Neighbor: O(n²) — suficiente para rotas com até 50 paradas
- 2-opt improvement: reduz distância total em ~15-20% vs. greedy puro
- Resultado prático aceitável para MVP
- Facilmente substituível no futuro (porta de saída — `RouteOptimizationPort`)

## Consequências

- Distâncias calculadas são euclidianas (aproximação por haversine)
- Para rotas urbanas com muitas restrições de tráfego, o resultado pode ser subótimo
- A interface `RouteOptimizationPort` abstrai o algoritmo — substituição futura é transparente para o domínio
