package com.logicore.api.controller;

import com.logicore.api.dto.request.LoginRequest;
import com.logicore.api.dto.request.RegisterRequest;
import com.logicore.identity.application.usecase.LoginService;
import com.logicore.identity.application.usecase.RegisterOrganizationService;
import com.logicore.identity.infrastructure.security.JwtService;
import com.logicore.security.application.usecase.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;


@Tag(name = "Auth", description = "Registro de organização e autenticação JWT")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterOrganizationService registerService;
    private final LoginService loginService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    public AuthController(RegisterOrganizationService registerService, LoginService loginService,
                          RefreshTokenService refreshTokenService, JwtService jwtService) {
        this.registerService = registerService;
        this.loginService = loginService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Registrar organização e usuário admin")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest req) {
        var result = registerService.execute(new RegisterOrganizationService.Command(
                req.organizationName(), req.slug(), req.adminName(), req.adminEmail(), req.adminPassword()));
        return Map.of("organizationId", result.organizationId(), "adminUserId", result.adminUserId());
    }

    public record RefreshRequest(@NotBlank String refreshToken) {}

    @Operation(summary = "Troca refresh token por novo par de tokens (rotation)")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(
            @Valid @RequestBody RefreshRequest req, HttpServletRequest httpRequest) {
        String userAgent = httpRequest.getHeader("User-Agent");
        String ip = httpRequest.getRemoteAddr();
        String newRefresh = refreshTokenService.rotateRefreshToken(req.refreshToken(), userAgent, ip);
        UUID userId = refreshTokenService.validateAndGetUserId(newRefresh);
        String newAccess = jwtService.generateRefreshToken(userId);
        return ResponseEntity.ok(Map.of("accessToken", newAccess, "refreshToken", newRefresh));
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
