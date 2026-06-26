package com.logicore.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PlanRouteRequest(
        @NotNull UUID vehicleId,
        @NotNull @Size(min = 1) List<UUID> orderIds,
        @NotNull Double depotLat,
        @NotNull Double depotLng,
        @NotNull LocalDate plannedDate,
        String strategyIdentifier,
        Map<String, Double> strategyWeights,
        Double maxWeightKg,
        Double avgSpeedKmh,
        Integer stopDurationMinutes,
        Boolean hardTimeWindows
) {}
