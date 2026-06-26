package com.logicore.api.controller;

import com.logicore.api.dto.request.PlanRouteRequest;
import com.logicore.api.dto.request.SimulateRequest;
import com.logicore.api.dto.response.MapRouteDTO;
import com.logicore.planning.application.usecase.PlanRouteService;
import com.logicore.planning.domain.strategy.StrategyConfig;
import com.logicore.planning.domain.valueobject.RouteConstraints;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import com.logicore.shared.infrastructure.tenant.TenantContext;
import com.logicore.simulation.application.usecase.CompareStrategiesService;
import com.logicore.simulation.application.usecase.WhatIfService;
import com.logicore.simulation.domain.valueobject.ImpactReport;
import com.logicore.simulation.domain.valueobject.ScenarioComparison;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/routes")
public class RouteController {

    private final PlanRouteService planRouteService;
    private final CompareStrategiesService compareStrategiesService;
    private final WhatIfService whatIfService;

    public RouteController(PlanRouteService planRouteService,
                            CompareStrategiesService compareStrategiesService,
                            WhatIfService whatIfService) {
        this.planRouteService = planRouteService;
        this.compareStrategiesService = compareStrategiesService;
        this.whatIfService = whatIfService;
    }

    @PostMapping("/plan")
    @ResponseStatus(HttpStatus.CREATED)
    public MapRouteDTO plan(@Valid @RequestBody PlanRouteRequest req) {
        UUID orgId = TenantContext.get();

        RouteConstraints constraints = buildConstraints(req.maxWeightKg(), req.avgSpeedKmh(),
                req.stopDurationMinutes(), req.hardTimeWindows());

        StrategyConfig config = req.strategyWeights() != null
                ? new StrategyConfig(req.strategyWeights(), 1000, Map.of())
                : StrategyConfig.defaults();

        var result = planRouteService.execute(new PlanRouteService.Command(
                orgId, req.vehicleId(), req.orderIds(),
                new GeoCoordinate(req.depotLat(), req.depotLng()),
                req.plannedDate(), req.strategyIdentifier(), config, constraints));

        return MapRouteDTO.from(result.routeId(), req.depotLat(), req.depotLng(), result.decisionResult());
    }

    @PostMapping("/simulate")
    public ScenarioComparison simulate(@Valid @RequestBody SimulateRequest req) {
        UUID orgId = TenantContext.get();

        RouteConstraints constraints = buildConstraints(req.maxWeightKg(), req.avgSpeedKmh(), null, null);

        return compareStrategiesService.execute(new CompareStrategiesService.Command(
                orgId, req.vehicleId(), req.orderIds(),
                new GeoCoordinate(req.depotLat(), req.depotLng()),
                constraints, req.strategyIdentifiers()));
    }

    @PostMapping("/{routeId}/what-if/{orderId}")
    public ImpactReport whatIf(@PathVariable UUID routeId, @PathVariable UUID orderId) {
        return whatIfService.execute(new WhatIfService.Command(TenantContext.get(), routeId, orderId));
    }

    private RouteConstraints buildConstraints(Double maxWeight, Double avgSpeed,
                                               Integer stopDuration, Boolean hardWindows) {
        if (maxWeight == null && avgSpeed == null && stopDuration == null && hardWindows == null) {
            return null; // will use defaults in service
        }
        return new RouteConstraints(
                maxWeight != null ? maxWeight : 1000.0,
                avgSpeed != null ? avgSpeed : 40.0,
                stopDuration != null ? stopDuration : 10,
                hardWindows != null && hardWindows
        );
    }
}
