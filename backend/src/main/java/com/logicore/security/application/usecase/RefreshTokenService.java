package com.logicore.security.application.usecase;

import com.logicore.identity.infrastructure.security.JwtService;
import com.logicore.security.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import com.logicore.security.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {

    private final RefreshTokenJpaRepository repository;
    private final JwtService jwtService;
    private final long refreshExpirationMs;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenJpaRepository repository,
            JwtService jwtService,
            @Value("${app.security.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
        this.repository = repository;
        this.jwtService = jwtService;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public record TokenPair(String accessToken, String refreshToken) {}

    public String createRefreshToken(UUID userId, UUID organizationId, String userAgent, String ipAddress) {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.setUserId(userId);
        entity.setOrganizationId(organizationId);
        entity.setTokenHash(hash(rawToken));
        entity.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        entity.setRevoked(false);
        entity.setUserAgent(userAgent);
        entity.setIpAddress(ipAddress);
        entity.setCreatedAt(Instant.now());
        repository.save(entity);

        return rawToken;
    }

    public String rotateRefreshToken(String rawToken, String userAgent, String ipAddress) {
        RefreshTokenJpaEntity existing = repository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new BadCredentialsException("Refresh token inválido"));

        if (existing.isRevoked()) {
            // Token reuse detected — revoke all tokens for this user
            repository.revokeAllForUser(existing.getUserId(), Instant.now());
            throw new BadCredentialsException("Refresh token reutilizado — todos os tokens revogados");
        }
        if (existing.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token expirado");
        }

        existing.setRevoked(true);
        existing.setRevokedAt(Instant.now());
        repository.save(existing);

        return createRefreshToken(existing.getUserId(), existing.getOrganizationId(), userAgent, ipAddress);
    }

    public UUID validateAndGetUserId(String rawToken) {
        RefreshTokenJpaEntity entity = repository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new BadCredentialsException("Refresh token inválido"));
        if (entity.isRevoked() || entity.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token expirado ou revogado");
        }
        return entity.getUserId();
    }

    public void revokeAllForUser(UUID userId) {
        repository.revokeAllForUser(userId, Instant.now());
    }

    @Transactional
    public void cleanupExpired() {
        repository.deleteExpiredAndRevoked(Instant.now());
    }

    private static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao hash token", e);
        }
    }
}
