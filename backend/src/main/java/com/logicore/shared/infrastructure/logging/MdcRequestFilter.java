package com.logicore.shared.infrastructure.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class MdcRequestFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";
    private static final String ORGANIZATION_ID = "organizationId";
    private static final String USER_ID = "userId";
    private static final String METHOD = "method";
    private static final String PATH = "path";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String requestId = request.getHeader("X-Request-ID");
            if (requestId == null || requestId.isBlank()) {
                requestId = UUID.randomUUID().toString();
            }

            MDC.put(REQUEST_ID, requestId);
            MDC.put(METHOD, request.getMethod());
            MDC.put(PATH, request.getRequestURI());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getDetails() instanceof java.util.Map<?, ?> details) {
                Object orgId = details.get("organizationId");
                Object userId = details.get("userId");
                if (orgId != null) MDC.put(ORGANIZATION_ID, orgId.toString());
                if (userId != null) MDC.put(USER_ID, userId.toString());
            }

            response.setHeader("X-Request-ID", requestId);
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
