package com.logicore.events.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record LogiCoreEvent(
        Long sequenceId,
        UUID eventId,
        UUID organizationId,
        String aggregateType,
        UUID aggregateId,
        String eventType,
        int eventVersion,
        Map<String, Object> payload,
        Map<String, Object> metadata,
        Instant occurredAt
) {
    public static final class Types {
        public static final String ROUTE_CREATED         = "route.created";
        public static final String ROUTE_OPTIMIZED       = "route.optimized";
        public static final String ROUTE_COMPLETED       = "route.completed";
        public static final String STOP_COMPLETED        = "route.stop.completed";
        public static final String BENCHMARK_RUN         = "benchmark.run";
        public static final String API_KEY_CREATED       = "security.api_key.created";
        public static final String API_KEY_REVOKED       = "security.api_key.revoked";
        private Types() {}
    }

    public static final class Aggregates {
        public static final String ROUTE        = "Route";
        public static final String BENCHMARK    = "Benchmark";
        public static final String API_KEY      = "ApiKey";
        private Aggregates() {}
    }
}
