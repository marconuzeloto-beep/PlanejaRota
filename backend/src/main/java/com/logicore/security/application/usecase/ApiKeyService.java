package com.logicore.security.application.usecase;

import com.logicore.security.infrastructure.persistence.entity.ApiKeyJpaEntity;
import com.logicore.security.infrastructure.persistence.repository.ApiKeyJpaRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ApiKeyService {

    private final ApiKeyJpaRepository repository;
    private final SecureRandom random = new SecureRandom();

    public ApiKeyService(ApiKeyJpaRepository repository) {
        this.repository = repository;
    }

    public record CreateResult(String rawKey, ApiKeyJpaEntity entity) {}

    public record ApiKeyInfo(UUID id, String name, String keyPrefix, String role,
                              boolean active, Instant createdAt, Instant lastUsedAt, Instant expiresAt) {}

    public CreateResult create(UUID organizationId, String name, String role, UUID createdBy, Instant expiresAt) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String rawKey = "lk_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String prefix = rawKey.substring(0, Math.min(8, rawKey.length()));

        ApiKeyJpaEntity entity = new ApiKeyJpaEntity();
        entity.setOrganizationId(organizationId);
        entity.setName(name);
        entity.setKeyPrefix(prefix);
        entity.setKeyHash(hash(rawKey));
        entity.setRole(role != null ? role : "API_USER");
        entity.setActive(true);
        entity.setExpiresAt(expiresAt);
        entity.setCreatedBy(createdBy);
        entity.setCreatedAt(Instant.now());
        repository.save(entity);

        return new CreateResult(rawKey, entity);
    }

    public Optional<ApiKeyJpaEntity> validateAndGet(String rawKey) {
        return repository.findByKeyHash(hash(rawKey))
                .filter(ApiKeyJpaEntity::isActive)
                .filter(k -> k.getExpiresAt() == null || k.getExpiresAt().isAfter(Instant.now()))
                .map(k -> {
                    repository.updateLastUsed(k.getId(), Instant.now());
                    return k;
                });
    }

    public List<ApiKeyInfo> list(UUID organizationId) {
        return repository.findByOrganizationIdAndActiveTrue(organizationId).stream()
                .map(k -> new ApiKeyInfo(k.getId(), k.getName(), k.getKeyPrefix(), k.getRole(),
                                         k.isActive(), k.getCreatedAt(), k.getLastUsedAt(), k.getExpiresAt()))
                .toList();
    }

    public void revoke(UUID organizationId, UUID keyId) {
        ApiKeyJpaEntity entity = repository.findById(keyId)
                .filter(k -> k.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new BadCredentialsException("API key não encontrada"));
        entity.setActive(false);
        entity.setRevokedAt(Instant.now());
        repository.save(entity);
    }

    private static String hash(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao hash API key", e);
        }
    }
}
