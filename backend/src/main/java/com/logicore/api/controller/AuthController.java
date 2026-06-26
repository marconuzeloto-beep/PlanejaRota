package com.logicore.api.controller;

import com.logicore.api.dto.request.LoginRequest;
import com.logicore.api.dto.request.RegisterRequest;
import com.logicore.identity.application.usecase.LoginService;
import com.logicore.identity.application.usecase.RegisterOrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Auth", description = "Registro de organização e autenticação JWT")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterOrganizationService registerService;
    private final LoginService loginService;

    public AuthController(RegisterOrganizationService registerService, LoginService loginService) {
        this.registerService = registerService;
        this.loginService = loginService;
    }

    @Operation(summary = "Registrar organização e usuário admin")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest req) {
        var result = registerService.execute(new RegisterOrganizationService.Command(
                req.organizationName(), req.slug(), req.adminName(), req.adminEmail(), req.adminPassword()));
        return Map.of("organizationId", result.organizationId(), "adminUserId", result.adminUserId());
    }

    @Operation(summary = "Login → retorna JWT de acesso e refresh token")
    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest req) {
        var result = loginService.execute(new LoginService.Command(req.slug(), req.email(), req.password()));
        return Map.of(
                "accessToken", result.accessToken(),
                "refreshToken", result.refreshToken(),
                "userId", result.userId(),
                "organizationId", result.organizationId(),
                "role", result.role()
        );
    }
}
