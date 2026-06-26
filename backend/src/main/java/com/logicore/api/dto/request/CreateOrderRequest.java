package com.logicore.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull UUID customerId,
        @NotBlank String orderCode,
        String description,
        @NotNull @Positive double weightKg,
        BigDecimal declaredValueBrl,
        @NotNull LocalDate deliveryDate,
        String notes
) {}
