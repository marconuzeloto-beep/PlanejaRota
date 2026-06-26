package com.logicore.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateVehicleRequest(
        @NotBlank String licensePlate,
        @NotBlank String model,
        @NotBlank String type,
        @NotNull @Positive double capacityKg,
        BigDecimal costPerKmBrl,
        String notes
) {}
