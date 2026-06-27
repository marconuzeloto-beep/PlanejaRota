package com.logicore.security.api;

import com.logicore.security.application.usecase.ApiKeyService;
import com.logicore.security.application.usecase.AuditService;
import com.logicore.shared.infrastructure.tenant.TenantContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/security")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final AuditService auditService;

    public ApiKeyController(ApiKeyService apiKeyService, AuditService auditService) {
        this.apiKeyService = apiKeyService;
        this.auditService = auditService;
    }

    public record CreateApiKeyRequest(
            @NotBlank String name,
            String role,
            Long expiresInDays
    ) {}

    public record CreateApiKeyResponse(
            UUID id,
            String name,
            String role,
            String rawKey,
            String keyPrefix,
            Instant createdAt,
            Instant expiresAt
    ) {}

    @PostMapping("/api-keys")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreateApiKeyResponse> create(
            @Valid @RequestBody CreateApiKeyRequest req,
            @RequestAttribute(name = "userId", required = false) UUID userId) {
        UUID orgId = TenantContext.get();
        Instant expiresAt = req.expiresInDays() != null
                ? Instant.now().plusSeconds(req.expiresInDays() * 86400L)
                : null;

        var result = apiKeyService.create(orgId, req.name(), req.role(), userId, expiresAt);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateApiKeyResponse(
                result.entity().getId(),
                result.entity().getName(),
                result.entity().getRole(),
                result.rawKey(),
                result.entity().getKeyPrefix(),
                result.entity().getCreatedAt(),
                result.entity().getExpiresAt()
        ));
    }

    @GetMapping("/api-keys")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApiKeyService.ApiKeyInfo>> list() {
        return ResponseEntity.ok(apiKeyService.list(TenantContext.get()));
    }

    @DeleteMapping("/api-keys/{keyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> revoke(@PathVariable UUID keyId) {
        apiKeyService.revoke(TenantContext.get(), keyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audit-log")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditService.AuditEntry>> auditLog(
            @RequestParam(required = false) UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                auditService.list(TenantContext.get(), userId, PageRequest.of(page, size)));
    }
}
