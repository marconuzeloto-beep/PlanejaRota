-- ============================================================
-- LogiCore — Benchmark Engine Schema
-- Flyway V2
-- ============================================================

CREATE TABLE benchmark_entries (
    id                    UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id       UUID         NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    strategy_identifier   VARCHAR(100) NOT NULL,
    strategy_type         VARCHAR(100) NOT NULL,
    order_count           INT          NOT NULL,
    execution_time_ms     BIGINT       NOT NULL,
    total_distance_km     DOUBLE PRECISION NOT NULL,
    total_time_minutes    INT          NOT NULL,
    capacity_usage_pct    DOUBLE PRECISION NOT NULL,
    stops_with_violations INT          NOT NULL DEFAULT 0,
    feasible              BOOLEAN      NOT NULL,
    route_score           DOUBLE PRECISION,
    score_grade           VARCHAR(2),
    two_opt_applied       BOOLEAN      NOT NULL DEFAULT FALSE,
    two_opt_improvement_km DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    memory_bytes          BIGINT,
    notes                 TEXT,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_benchmark_org        ON benchmark_entries(organization_id);
CREATE INDEX idx_benchmark_strategy   ON benchmark_entries(strategy_identifier);
CREATE INDEX idx_benchmark_created    ON benchmark_entries(created_at DESC);
CREATE INDEX idx_benchmark_score      ON benchmark_entries(route_score DESC);
