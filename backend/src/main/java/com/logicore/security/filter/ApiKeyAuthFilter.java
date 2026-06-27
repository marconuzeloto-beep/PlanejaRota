package com.logicore.security.filter;

import com.logicore.security.application.usecase.ApiKeyService;
import com.logicore.security.infrastructure.persistence.entity.ApiKeyJpaEntity;
import com.logicore.shared.infrastructure.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Intercepts requests with X-API-Key header and authenticates them as
 * an API key principal, bypassing JWT authentication.
 */
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-API-Key";

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String key = request.getHeader(HEADER);
        if (key != null && !key.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            apiKeyService.validateAndGet(key).ifPresent(entity -> {
                var auth = new UsernamePasswordAuthenticationToken(
                        "api-key:" + entity.getId(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + entity.getRole()))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                TenantContext.set(entity.getOrganizationId());
            });
        }
        filterChain.doFilter(request, response);
    }
}
