-- =====================================================================
-- V3: Enterprise Schema — Refresh Tokens, API Keys, Audit, Events, Analytics
-- =====================================================================

-- Refresh Tokens
CREATE TABLE refresh_tokens (
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    organization_id UUID        NOT NULL,
    user_id         UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash      VARCHAR(64) NOT NULL UNIQUE,
    expires_at      TIMESTAMPTZ NOT NULL,
    revoked         BOOLEAN     NOT NULL DEFAULT FALSE,
    revoked_at      TIMESTAMPTZ,
    user_agent      VARCHAR(512),
    ip_address      VARCHAR(45),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_user_id    ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

-- API Keys
CREATE TABLE api_keys (
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    organization_id UUID        NOT NULL,
    name            VARCHAR(128) NOT NULL,
    key_prefix      VARCHAR(8)  NOT NULL,
    key_hash        VARCHAR(64) NOT NULL UNIQUE,
    role            VARCHAR(32) NOT NULL DEFAULT 'API_USER',
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    expires_at      TIMESTAMPTZ,
    last_used_at    TIMESTAMPTZ,
    created_by      UUID        REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    revoked_at      TIMESTAMPTZ
);

CREATE INDEX idx_api_keys_key_hash        ON api_keys(key_hash);
CREATE INDEX idx_api_keys_organization_id ON api_keys(organization_id);

-- Audit Log
CREATE TABLE audit_log (
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    organization_id UUID,
    user_id         UUID,
    action          VARCHAR(64) NOT NULL,
    resource_type   VARCHAR(64),
    resource_id     VARCHAR(128),
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(512),
    request_path    VARCHAR(512),
    http_method     VARCHAR(8),
    http_status     INTEGER,
    duration_ms     BIGINT,
    payload_summary TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_log_org_id     ON audit_log(organization_id);
CREATE INDEX idx_audit_log_user_id    ON audit_log(user_id);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);
CREATE INDEX idx_audit_log_action     ON audit_log(action);

-- Event Store
CREATE TABLE logicore_events (
    id              BIGSERIAL   NOT NULL PRIMARY KEY,
    event_id        UUID        NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    organization_id UUID        NOT NULL,
    aggregate_type  VARCHAR(64) NOT NULL,
    aggregate_id    UUID        NOT NULL,
    event_type      VARCHAR(128) NOT NULL,
    event_version   INTEGER     NOT NULL DEFAULT 1,
    payload         JSONB       NOT NULL,
    metadata        JSONB,
    occurred_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_events_aggregate ON logicore_events(aggregate_type, aggregate_id);
CREATE INDEX idx_events_org_id    ON logicore_events(organization_id);
CREATE INDEX idx_events_type      ON logicore_events(event_type);
CREATE INDEX idx_events_occurred  ON logicore_events(occurred_at);

-- Analytics Daily Snapshots
CREATE TABLE analytics_daily_snapshots (
    id                    UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    organization_id       UUID        NOT NULL,
    snapshot_date         DATE        NOT NULL,
    total_routes          INTEGER     NOT NULL DEFAULT 0,
    completed_routes      INTEGER     NOT NULL DEFAULT 0,
    total_distance_km     NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_duration_min    NUMERIC(12,2) NOT NULL DEFAULT 0,
    avg_route_score       NUMERIC(5,4),
    total_orders_planned  INTEGER     NOT NULL DEFAULT 0,
    total_orders_delivered INTEGER    NOT NULL DEFAULT 0,
    fuel_consumption_l    NUMERIC(10,2),
    co2_kg                NUMERIC(10,2),
    on_time_deliveries    INTEGER     NOT NULL DEFAULT 0,
    constraint_violations INTEGER     NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(organization_id, snapshot_date)
);

CREATE INDEX idx_analytics_org_date ON analytics_daily_snapshots(organization_id, snapshot_date DESC);

-- Live Executions
CREATE TABLE live_executions (
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    organization_id UUID        NOT NULL,
    route_id        UUID,
    vehicle_id      UUID        NOT NULL REFERENCES vehicles(id),
    driver_name     VARCHAR(128),
    status          VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    current_lat     NUMERIC(10,7),
    current_lng     NUMERIC(10,7),
    current_stop    INTEGER     NOT NULL DEFAULT 0,
    total_stops     INTEGER     NOT NULL DEFAULT 0,
    planned_distance_km NUMERIC(10,2),
    actual_distance_km  NUMERIC(10,2) NOT NULL DEFAULT 0,
    eta_minutes     INTEGER,
    deviation_km    NUMERIC(8,2),
    started_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_live_executions_org_status ON live_executions(organization_id, status);
CREATE INDEX idx_live_executions_vehicle    ON live_executions(vehicle_id);
