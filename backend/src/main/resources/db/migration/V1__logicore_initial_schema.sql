-- ============================================================
-- LogiCore — Schema Inicial Completo
-- Flyway V1
-- ============================================================

-- Extensões obrigatórias
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- IDENTITY & ORGANIZATION CONTEXT
-- ============================================================

CREATE TABLE organizations (
    id          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(100) NOT NULL UNIQUE,   -- identificador público imutável
    plan        VARCHAR(50)  NOT NULL DEFAULT 'FREE',
    status      VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    settings    JSONB        NOT NULL DEFAULT '{}',  -- OrganizationSettings VO
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Email é único por organização, não globalmente
-- Mesmo usuário pode ter conta em duas empresas diferentes
CREATE TABLE users (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID        NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    role            VARCHAR(50)  NOT NULL DEFAULT 'VIEWER',
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (organization_id, email)
);

-- ============================================================
-- CORE DOMAIN CONTEXT
-- ============================================================

CREATE TABLE customers (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID        NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(50),
    -- Address Value Object (flattened — evita JOIN desnecessário)
    street          VARCHAR(255) NOT NULL,
    address_number  VARCHAR(20)  NOT NULL,
    complement      VARCHAR(100),
    neighborhood    VARCHAR(100),
    city            VARCHAR(100) NOT NULL,
    state           VARCHAR(50)  NOT NULL,
    zip_code        VARCHAR(20)  NOT NULL,
    country         VARCHAR(50)  NOT NULL DEFAULT 'Brasil',
    -- GeoCoordinate como PostGIS Point SRID 4326
    -- Obrigatório — invariante do domínio: Cliente sem localização é inválido
    location        GEOMETRY(Point, 4326) NOT NULL,
    -- TimeWindow Value Object (nullable — cliente pode não ter janela preferencial)
    tw_open         TIME,
    tw_close        TIME,
    -- Priority Value Object [1..5]
    priority        SMALLINT     NOT NULL DEFAULT 3 CHECK (priority BETWEEN 1 AND 5),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE vehicles (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID        NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    license_plate   VARCHAR(20)  NOT NULL,
    model           VARCHAR(100) NOT NULL,
    type            VARCHAR(50)  NOT NULL,   -- VehicleType enum
    capacity_kg     NUMERIC(10,2) NOT NULL CHECK (capacity_kg > 0),
    cost_per_km     NUMERIC(10,4),           -- Money VO — nullable (org pode não usar custo)
    status          VARCHAR(50)  NOT NULL DEFAULT 'AVAILABLE',
    notes           TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (organization_id, license_plate)
);

CREATE TABLE delivery_orders (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID        NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    customer_id     UUID        NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    order_code      VARCHAR(100) NOT NULL,
    description     TEXT,
    weight_kg       NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (weight_kg >= 0),
    declared_value  NUMERIC(15,2),
    delivery_date   DATE         NOT NULL,
    -- TimeWindow específico do pedido (sobrescreve o do cliente se presente)
    tw_open         TIME,
    tw_close        TIME,
    status          VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    notes           TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (organization_id, order_code)
);

-- ============================================================
-- PLANNING ENGINE CONTEXT
-- ============================================================

CREATE TABLE routes (
    id                      UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id         UUID        NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name                    VARCHAR(255) NOT NULL,
    vehicle_id              UUID        NOT NULL REFERENCES vehicles(id) ON DELETE RESTRICT,
    driver_id               UUID        REFERENCES users(id) ON DELETE SET NULL,
    depot_location          GEOMETRY(Point, 4326) NOT NULL,
    return_depot_location   GEOMETRY(Point, 4326),   -- ponto de retorno se diferente
    scheduled_date          DATE         NOT NULL,
    departure_time          TIME         NOT NULL DEFAULT '08:00:00',
    status                  VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    -- FK para o cenário selecionado (adicionada após route_scenarios ser criada)
    selected_scenario_id    UUID,
    -- Optimistic locking — evita conflito de dois gestores editando a mesma rota
    version                 INTEGER      NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Cada parada = um pedido em uma posição específica da rota
-- decision_step armazena o DecisionStep VO (razão da escolha desta posição)
CREATE TABLE route_stops (
    id                          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id             UUID        NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    route_id                    UUID        NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    order_id                    UUID        NOT NULL REFERENCES delivery_orders(id) ON DELETE RESTRICT,
    sequence_number             SMALLINT    NOT NULL,
    status                      VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    estimated_arrival_time      TIME,
    -- DecisionStep VO completo em JSONB — inclui razão, score, distância parcial
    decision_step               JSONB,
    actual_arrival_time         TIME,
    failure_reason_type         VARCHAR(50),
    failure_reason_description  TEXT,
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (route_id, order_id),        -- pedido não pode aparecer 2x na mesma rota
    UNIQUE (route_id, sequence_number)  -- sequência única e contígua por rota
);

-- ============================================================
-- SIMULATION ENGINE CONTEXT
-- ============================================================

-- IMUTÁVEL após criação — nenhum campo muda exceto status
-- context_snapshot = estado dos dados no momento da simulação (Vehicle, Orders)
-- decision_result = saída completa do Planning Engine (DecisionResult VO)
-- Não há updated_at — imutabilidade é uma invariante
CREATE TABLE route_scenarios (
    id               UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id  UUID        NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    route_id         UUID        REFERENCES routes(id) ON DELETE SET NULL,
    strategy_type    VARCHAR(50)  NOT NULL,
    strategy_config  JSONB        NOT NULL DEFAULT '{}',
    decision_result  JSONB        NOT NULL,  -- DecisionResult completo
    context_snapshot JSONB        NOT NULL,  -- ScenarioContext snapshot
    status           VARCHAR(50)  NOT NULL DEFAULT 'CANDIDATE',  -- CANDIDATE | SELECTED | DISCARDED
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    -- Sem updated_at: imutável por design
);

-- FK circular: routes → route_scenarios (adicionada após ambas as tabelas existirem)
ALTER TABLE routes
    ADD CONSTRAINT fk_routes_selected_scenario
    FOREIGN KEY (selected_scenario_id)
    REFERENCES route_scenarios(id)
    ON DELETE SET NULL;

-- ============================================================
-- EXECUTION ENGINE CONTEXT
-- ============================================================

CREATE TABLE route_executions (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID        NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    route_id        UUID        NOT NULL REFERENCES routes(id) ON DELETE RESTRICT,
    driver_id       UUID        NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    status          VARCHAR(50)  NOT NULL DEFAULT 'NOT_STARTED',
    started_at      TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    summary         JSONB,       -- ExecutionSummary VO (calculado ao concluir)
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Registro imutável de um evento de parada durante execução
-- UNIQUE (execution_id, stop_id): cada parada tem exatamente um desfecho
CREATE TABLE stop_events (
    id                          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id             UUID        NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    execution_id                UUID        NOT NULL REFERENCES route_executions(id) ON DELETE CASCADE,
    stop_id                     UUID        NOT NULL REFERENCES route_stops(id) ON DELETE RESTRICT,
    type                        VARCHAR(50)  NOT NULL,   -- COMPLETED | FAILED | SKIPPED
    occurred_at                 TIMESTAMPTZ  NOT NULL,
    driver_notes                TEXT,
    failure_reason_type         VARCHAR(50),
    failure_reason_description  TEXT,
    UNIQUE (execution_id, stop_id)
);

-- ============================================================
-- AUDIT LOG (transversal)
-- ============================================================

-- organization_id SEM foreign key intencional:
-- logs devem sobreviver mesmo se org for deletada (retenção fiscal 5 anos)
CREATE TABLE audit_logs (
    id              UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID        NOT NULL,
    user_id         UUID,
    action          VARCHAR(100) NOT NULL,   -- ex: ROUTE_CREATED, ORDER_CANCELLED
    entity_type     VARCHAR(100) NOT NULL,   -- ex: Route, DeliveryOrder
    entity_id       UUID,
    payload         JSONB,                   -- dados relevantes no momento da ação
    ip_address      VARCHAR(50),
    occurred_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ============================================================
-- ÍNDICES
-- Estratégia: indexar todas as queries frequentes identificadas nos use cases
-- ============================================================

-- Identity
CREATE INDEX idx_users_org_id          ON users(organization_id);
CREATE INDEX idx_users_org_email       ON users(organization_id, email);
CREATE INDEX idx_users_active          ON users(organization_id, active);

-- Customers
CREATE INDEX idx_customers_org_id      ON customers(organization_id);
CREATE INDEX idx_customers_active      ON customers(organization_id, active);
-- GIST: índice espacial obrigatório para ST_DWithin (busca por proximidade)
CREATE INDEX idx_customers_location    ON customers USING GIST(location);

-- Vehicles
CREATE INDEX idx_vehicles_org_id       ON vehicles(organization_id);
CREATE INDEX idx_vehicles_status       ON vehicles(organization_id, status);

-- Orders
CREATE INDEX idx_orders_org_id         ON delivery_orders(organization_id);
CREATE INDEX idx_orders_customer       ON delivery_orders(customer_id);
CREATE INDEX idx_orders_date_status    ON delivery_orders(organization_id, delivery_date, status);

-- Routes
CREATE INDEX idx_routes_org_id         ON routes(organization_id);
CREATE INDEX idx_routes_vehicle        ON routes(vehicle_id);
CREATE INDEX idx_routes_date           ON routes(organization_id, scheduled_date);
CREATE INDEX idx_routes_status         ON routes(organization_id, status);

-- Route Stops
CREATE INDEX idx_route_stops_route     ON route_stops(route_id);
CREATE INDEX idx_route_stops_order     ON route_stops(order_id);

-- Scenarios
CREATE INDEX idx_scenarios_org_id      ON route_scenarios(organization_id);
CREATE INDEX idx_scenarios_route       ON route_scenarios(route_id);
CREATE INDEX idx_scenarios_status      ON route_scenarios(organization_id, status);

-- Executions
CREATE INDEX idx_executions_route      ON route_executions(route_id);
CREATE INDEX idx_executions_driver     ON route_executions(driver_id);

-- Stop Events
CREATE INDEX idx_stop_events_execution ON stop_events(execution_id);

-- Audit Logs
CREATE INDEX idx_audit_org_id          ON audit_logs(organization_id);
CREATE INDEX idx_audit_entity          ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_occurred        ON audit_logs(occurred_at DESC);
