# PlanejaRota — Architecture Decision Record (Overview)

## 1. Visão do Sistema

PlanejaRota é uma plataforma de planejamento logístico inteligente que permite vendedores e gestores de frota otimizarem rotas de entrega considerando restrições reais do mundo logístico: janelas de tempo, capacidade de veículo, prioridade de cliente, custo operacional e condições de tráfego.

---

## 2. Objetivos Arquiteturais

| Atributo de Qualidade | Meta |
|---|---|
| Manutenibilidade | Mudanças em regras de negócio não propagam para infra |
| Testabilidade | Domínio testável sem banco ou framework |
| Escalabilidade | Módulos independentes e stateless |
| Segurança | JWT + RBAC, sem dados sensíveis em log |
| Performance | Queries geoespaciais via PostGIS, paginação padrão |
| Portabilidade | Containers Docker, sem vendor lock-in |

---

## 3. Arquitetura em Camadas (Clean Architecture)

```
┌─────────────────────────────────────────────────────┐
│                   Interfaces Layer                   │
│   REST Controllers · DTOs · Exception Handlers      │
└───────────────────────┬─────────────────────────────┘
                        │ (depende apenas de →)
┌───────────────────────▼─────────────────────────────┐
│                 Application Layer                    │
│   Use Cases · Ports (Input/Output) · App Services   │
└───────────────────────┬─────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────┐
│                   Domain Layer                       │
│   Entities · Value Objects · Aggregates             │
│   Domain Services · Domain Events · Exceptions      │
│   Repository Interfaces (ports)                     │
└─────────────────────────────────────────────────────┘
                        ▲
┌───────────────────────┴─────────────────────────────┐
│               Infrastructure Layer                  │
│   JPA Entities · Spring Data Repos · REST Clients  │
│   Security Config · External Services               │
└─────────────────────────────────────────────────────┘
```

**Regra de dependência:** as setas apontam para dentro. O domínio não conhece nenhuma camada externa.

---

## 4. Bounded Contexts (DDD)

### 4.1 Identity Context
Responsável por: usuários, autenticação, autorização, perfis de acesso.

Entidades principais: `User`, `Role`, `Permission`
Eventos: `UserRegistered`, `UserActivated`

### 4.2 Fleet Context
Responsável por: veículos, motoristas, capacidades operacionais.

Entidades principais: `Vehicle`, `Driver`, `VehicleCapacity`
Eventos: `VehicleAssigned`, `DriverAvailable`

### 4.3 Route Planning Context (Core Domain)
Responsável por: clientes, pedidos, planejamento e otimização de rotas.

Entidades principais: `Customer`, `DeliveryOrder`, `Route`, `RouteStop`
Aggregates: `Route` (aggregate root com `RouteStop` como entidades filhas)
Value Objects: `Address`, `GeoCoordinate`, `TimeWindow`, `Capacity`
Eventos: `RouteCreated`, `RouteOptimized`, `StopCompleted`

### 4.4 Execution Context
Responsável por: execução em tempo real, tracking, confirmações.

Entidades: `DeliveryExecution`, `LocationUpdate`

### 4.5 Analytics Context
Responsável por: KPIs, relatórios, exportações.

---

## 5. Modelo de Dados Conceitual

```
User ──────────┐
               │ manages
Driver ────────┤
               │
Vehicle ───────┤ assigned to
               ▼
            Route ──── RouteStop ──── DeliveryOrder ──── Customer
               │            │
               │       GeoCoordinate
               │
            TimeWindow
```

---

## 6. Stack Tecnológica

### Backend
| Componente | Tecnologia | Justificativa |
|---|---|---|
| Framework | Spring Boot 3.x | Ecosistema maduro, suporte nativo a GraalVM nativo |
| Segurança | Spring Security + JWT | Stateless, escalável horizontalmente |
| Persistência | Spring Data JPA + Hibernate | Produtividade com controle fino quando necessário |
| Banco | PostgreSQL 15 + PostGIS | Queries geoespaciais nativas, ACID, open source |
| Migração | Flyway | Versionamento de schema com rastreabilidade |
| Testes | JUnit 5 + Mockito + Testcontainers | Domínio unitário, integração com banco real |
| Build | Maven | Convencional para Java enterprise |

### Frontend
| Componente | Tecnologia | Justificativa |
|---|---|---|
| Framework | React 18 + TypeScript | Type safety, ecosistema, performance |
| Estado | Zustand | Simples, sem boilerplate Redux |
| Data Fetching | React Query (TanStack) | Cache, loading states, invalidação automática |
| Mapa | Leaflet + React-Leaflet | Open source, integra com OSM |
| Dados cartográficos | OpenStreetMap | Gratuito, qualidade excelente |
| Roteamento | React Router v6 | Padrão da indústria |
| Forms | React Hook Form + Zod | Performance + validação type-safe |
| UI | Tailwind CSS + shadcn/ui | Design system sem vendor lock-in |
| Testes | Vitest + Testing Library | Rápido, compatível com Vite |
| Build | Vite | Mais rápido que CRA, HMR excelente |

### Infraestrutura
| Componente | Tecnologia | Justificativa |
|---|---|---|
| Container | Docker + Docker Compose | Ambiente reproduzível |
| CI/CD | GitHub Actions | Integrado ao repositório |

---

## 7. Fluxo Principal: Planejamento de Rota

```
1. Usuário cria pedidos de entrega (Customer + Address + TimeWindow)
2. Usuário seleciona veículo e motorista
3. Sistema calcula rota otimizada via algoritmo (nearest neighbor + 2-opt)
4. Sistema exibe rota no mapa (Leaflet + OSM)
5. Usuário ajusta manualmente se necessário
6. Rota é salva e enviada ao motorista
7. Motorista executa e confirma cada parada
8. Sistema atualiza status em tempo real
```

---

## 8. Segurança

- Autenticação: JWT com refresh token rotation
- Autorização: RBAC (ADMIN, MANAGER, DRIVER, VIEWER)
- Senhas: BCrypt com fator de custo 12
- HTTPS obrigatório em produção
- Headers de segurança via Spring Security
- Sem dados sensíveis em logs
- Validação de entrada em todas as bordas (Bean Validation + Zod)

---

## 9. Decisões Arquiteturais Pendentes (ADRs)

- ADR-001: Algoritmo de otimização de rotas (implementação própria vs. OSRM)
- ADR-002: Estratégia de real-time (WebSocket vs. Server-Sent Events)
- ADR-003: Estratégia de notificação ao motorista (PWA Push vs. SMS)
