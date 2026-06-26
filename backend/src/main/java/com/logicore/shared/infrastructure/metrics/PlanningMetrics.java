package com.logicore.shared.infrastructure.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PlanningMetrics {

    private final Timer planRouteTimer;
    private final Timer simulationTimer;
    private final Timer whatIfTimer;

    public PlanningMetrics(MeterRegistry registry) {
        this.planRouteTimer = Timer.builder("logicore.planning.route.duration")
                .description("Time to plan a route")
                .register(registry);

        this.simulationTimer = Timer.builder("logicore.simulation.compare.duration")
                .description("Time to compare strategies in simulation")
                .register(registry);

        this.whatIfTimer = Timer.builder("logicore.simulation.whatif.duration")
                .description("Time to compute what-if scenario")
                .register(registry);
    }

    public void recordPlanRoute(long durationMs) {
        planRouteTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordSimulation(long durationMs) {
        simulationTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordWhatIf(long durationMs) {
        whatIfTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
}
