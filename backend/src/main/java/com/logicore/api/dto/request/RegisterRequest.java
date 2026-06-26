package com.logicore.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 2, max = 100) String organizationName,
        @NotBlank @Pattern(regexp = "[a-z0-9-]+", message = "Slug deve conter apenas letras minúsculas, números e hífens")
        String slug,
        @NotBlank @Size(min = 2, max = 100) String adminName,
        @NotBlank @Email String adminEmail,
        @NotBlank @Size(min = 8) String adminPassword
) {}
