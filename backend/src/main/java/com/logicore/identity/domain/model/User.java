package com.logicore.identity.domain.model;

import com.logicore.shared.domain.exception.BusinessRuleException;

import java.time.Instant;
import java.util.UUID;

public class User {

    private UUID id;
    private UUID organizationId;
    private String email;
    private String passwordHash;
    private String fullName;
    private Role role;
    private boolean active;
    private Instant lastLoginAt;
    private final Instant createdAt;

    private User(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public static User create(UUID organizationId, String email, String passwordHash,
                               String fullName, Role role) {
        if (organizationId == null) throw new IllegalArgumentException("OrganizationId é obrigatório");
        if (email == null || !email.contains("@")) throw new IllegalArgumentException("E-mail inválido");
        if (passwordHash == null || passwordHash.isBlank()) throw new IllegalArgumentException("Senha é obrigatória");
        if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("Nome é obrigatório");
        if (role == null) throw new IllegalArgumentException("Perfil é obrigatório");

        User user = new User(Instant.now());
        user.id             = UUID.randomUUID();
        user.organizationId = organizationId;
        user.email          = email.toLowerCase().trim();
        user.passwordHash   = passwordHash;
        user.fullName       = fullName.trim();
        user.role           = role;
        user.active         = true;
        return user;
    }

    public static User reconstitute(UUID id, UUID organizationId, String email,
                                     String passwordHash, String fullName,
                                     Role role, boolean active, Instant lastLoginAt, Instant createdAt) {
        User user = new User(createdAt);
        user.id             = id;
        user.organizationId = organizationId;
        user.email          = email;
        user.passwordHash   = passwordHash;
        user.fullName       = fullName;
        user.role           = role;
        user.active         = active;
        user.lastLoginAt    = lastLoginAt;
        return user;
    }

    public void recordLogin() {
        if (!active) throw new BusinessRuleException("Usuário inativo não pode fazer login");
        this.lastLoginAt = Instant.now();
    }

    public void deactivate() { this.active = false; }
    public void activate()   { this.active = true; }

    public void changeRole(Role newRole) {
        if (newRole == null) throw new IllegalArgumentException("Novo perfil é obrigatório");
        this.role = newRole;
    }

    public boolean canManageUsers()  { return role == Role.ADMIN; }
    public boolean canPlanRoutes()   { return role == Role.ADMIN || role == Role.MANAGER; }
    public boolean canExecuteRoutes(){ return role == Role.ADMIN || role == Role.MANAGER || role == Role.DRIVER; }

    public UUID getId()           { return id; }
    public UUID getOrganizationId(){ return organizationId; }
    public String getEmail()      { return email; }
    public String getPasswordHash(){ return passwordHash; }
    public String getFullName()   { return fullName; }
    public Role getRole()         { return role; }
    public boolean isActive()     { return active; }
    public Instant getLastLoginAt(){ return lastLoginAt; }
    public Instant getCreatedAt() { return createdAt; }
}
