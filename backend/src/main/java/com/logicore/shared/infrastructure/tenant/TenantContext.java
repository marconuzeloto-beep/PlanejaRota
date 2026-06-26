package com.logicore.shared.infrastructure.tenant;

import java.util.UUID;

/**
 * Armazena o organizationId do request atual em ThreadLocal.
 * Populado pelo TenantRequestFilter e lido pelo Hibernate Filter.
 * NUNCA deve ser lido dentro do domínio — apenas na infraestrutura.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_ORG = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(UUID organizationId) {
        CURRENT_ORG.set(organizationId);
    }

    public static UUID get() {
        UUID orgId = CURRENT_ORG.get();
        if (orgId == null)
            throw new IllegalStateException("TenantContext não inicializado para este thread — filtro de tenant ausente");
        return orgId;
    }

    public static boolean isPresent() {
        return CURRENT_ORG.get() != null;
    }

    /** Deve ser chamado ao final de cada request para evitar memory leak em thread pools. */
    public static void clear() {
        CURRENT_ORG.remove();
    }
}
