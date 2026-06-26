-- PlanejaRota — Schema inicial
-- Flyway migration V1

-- Extensão geoespacial do PostgreSQL
-- Necessária para colunas GEOMETRY e queries ST_*
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- IDENTITY CONTEXT
-- ============================================================

CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    full_name   VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL DEFAULT 'VIEWER',
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);

-- ============================================================
-- FLEET CONTEXT
-- ============================================================

CREATE TABLE vehicles (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    license_plate VARCHAR(20)  NOT NULL UNIQUE,
    model         VARCHAR(100) NOT NULL,
    type          VARCHAR(50)  NOT NULL,
    capacity_kg   NUMERIC(10,2) NOT NULL CHECK (capacity_kg > 0),
    status        VARCHAR(50)  NOT NULL DEFAULT 'AVAILABLE',
    notes         TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE drivers (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID REFERENCES users(id) ON DELETE SET NULL,
    full_name   VARCHAR(255) NOT NULL,
    license_number VARCHAR(30) NOT NULL UNIQUE,
    phone       VARCHAR(30),
    status      VARCHAR(50)  NOT NULL DEFAULT 'AVAILABLE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ============================================================
-- ROUTE PLANNING CONTEXT
-- ============================================================

CREATE TABLE customers (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(30),
    -- Address fields (value object flattened)
    street          VARCHAR(255) NOT NULL,
    address_number  VARCHAR(20)  NOT NULL,
    complement      VARCHAR(100),
    neighborhood    VARCHAR(100),
    city            VARCHAR(100) NOT NULL,
    state           VARCHAR(50)  NOT NULL,
    zip_code        VARCHAR(20)  NOT NULL,
    country         VARCHAR(50)  NOT NULL DEFAULT 'Brasil',
    -- Geolocation (PostGIS POINT)
    location        GEOMETRY(Point, 4326),
    -- Time window (value object flattened)
    tw_open         TIME,
    tw_close        TIME,
    priority        SMALLINT     NOT NULL DEFAULT 3 CHECK (priority BETWEEN 1 AND 5),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Índice espacial GIST — fundamental para queries de proximidade ST_DWithin
CREATE INDEX idx_customers_location ON customers USING GIST(location);

CREATE TABLE delivery_orders (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id     UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    order_code      VARCHAR(100) NOT NULL UNIQUE,
    description     TEXT,
    weight_kg       NUMERIC(10,2) NOT NULL CHECK (weight_kg >= 0),
    value           NUMERIC(15,2),
    delivery_date   DATE NOT NULL,
    -- Time window (pode diferir da preferência do cliente para este pedido específico)
    tw_open         TIME,
    tw_close        TIME,
    status          VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    notes           TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_delivery_orders_customer ON delivery_orders(customer_id);
CREATE INDEX idx_delivery_orders_date_status ON delivery_orders(delivery_date, status);

CREATE TABLE routes (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name                VARCHAR(255) NOT NULL,
    vehicle_id          UUID NOT NULL REFERENCES vehicles(id) ON DELETE RESTRICT,
    driver_id           UUID REFERENCES drivers(id) ON DELETE SET NULL,
    -- Ponto de partida (depósito/base)
    depot_location      GEOMETRY(Point, 4326) NOT NULL,
    scheduled_date      DATE        NOT NULL,
    departure_time      TIME        NOT NULL DEFAULT '08:00:00',
    status              VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    total_distance_km   NUMERIC(10,2),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_routes_date ON routes(scheduled_date);
CREATE INDEX idx_routes_vehicle ON routes(vehicle_id);
CREATE INDEX idx_routes_status ON routes(status);

CREATE TABLE route_stops (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    route_id            UUID NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    order_id            UUID NOT NULL REFERENCES delivery_orders(id) ON DELETE RESTRICT,
    sequence_number     SMALLINT    NOT NULL,
    status              VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    estimated_arrival   TIME,
    actual_arrival      TIME,
    failure_reason      TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (route_id, order_id),
    UNIQUE (route_id, sequence_number)
);

CREATE INDEX idx_route_stops_route ON route_stops(route_id);
