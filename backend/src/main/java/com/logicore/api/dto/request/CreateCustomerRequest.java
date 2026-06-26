package com.logicore.api.dto.request;

import jakarta.validation.constraints.*;

public record CreateCustomerRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Email String email,
        String phone,
        @NotNull Double latitude,
        @NotNull Double longitude,
        @Min(1) @Max(5) int priority,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String zipCode
) {}
