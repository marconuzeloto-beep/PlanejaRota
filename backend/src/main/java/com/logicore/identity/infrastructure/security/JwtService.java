package com.logicore.identity.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties properties;

    public String generateToken(UUID userId, UUID organizationId, String role) {
        return buildToken(userId.toString(), Map.of(
                "organizationId", organizationId.toString(),
                "role", role
        ), properties.expirationMs());
    }

    public String generateRefreshToken(UUID userId) {
        return buildToken(userId.toString(), Map.of(), properties.refreshExpirationMs());
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public UUID extractOrganizationId(String token) {
        String orgId = parseClaims(token).get("organizationId", String.class);
        if (orgId == null) throw new IllegalArgumentException("Token não contém organizationId");
        return UUID.fromString(orgId);
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private String buildToken(String subject, Map<String, Object> claims, long expirationMs) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey())
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(signingKey()).build()
                .parseSignedClaims(token).getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
