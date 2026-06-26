package com.logicore.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record SimulateRequest(
        @NotNull UUID vehicleId,
        @NotNull @Size(min = 1) List<UUID> orderIds,
        @NotNull Double depotLat,
        @NotNull Double depotLng,
        List<String> strategyIdentifiers,
        Double maxWeightKg,
        Double avgSpeedKmh
) {}
