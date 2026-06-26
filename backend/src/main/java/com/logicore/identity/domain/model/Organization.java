package com.logicore.identity.domain.model;

import com.logicore.shared.domain.exception.BusinessRuleException;

import java.time.Instant;
import java.util.UUID;

public class Organization {

    public enum Status { ACTIVE, SUSPENDED, CANCELLED }
    public enum Plan   { FREE, STARTER, PROFESSIONAL }

    private UUID id;
    private String name;
    private String slug;
    private Plan plan;
    private Status status;
    private final Instant createdAt;

    private Organization(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public static Organization create(String name, String slug) {
        validateName(name);
        validateSlug(slug);
        Organization org = new Organization(Instant.now());
        org.id     = UUID.randomUUID();
        org.name   = name.trim();
        org.slug   = slug.toLowerCase().trim();
        org.plan   = Plan.FREE;
        org.status = Status.ACTIVE;
        return org;
    }

    public static Organization reconstitute(UUID id, String name, String slug,
                                             Plan plan, Status status, Instant createdAt) {
        Organization org = new Organization(createdAt);
        org.id     = id;
        org.name   = name;
        org.slug   = slug;
        org.plan   = plan;
        org.status = status;
        return org;
    }

    public void suspend() {
        if (status == Status.CANCELLED)
            throw new BusinessRuleException("Organização cancelada não pode ser suspensa");
        this.status = Status.SUSPENDED;
    }

    public void reactivate() {
        if (status == Status.CANCELLED)
            throw new BusinessRuleException("Organização cancelada não pode ser reativada");
        this.status = Status.ACTIVE;
    }

    public boolean isActive() { return status == Status.ACTIVE; }

    private static void validateName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Nome da organização é obrigatório");
    }

    private static void validateSlug(String slug) {
        if (slug == null || slug.isBlank())
            throw new IllegalArgumentException("Slug é obrigatório");
        if (!slug.matches("[a-z0-9-]+"))
            throw new IllegalArgumentException("Slug deve conter apenas letras minúsculas, números e hífens");
    }

    public UUID getId()       { return id; }
    public String getName()   { return name; }
    public String getSlug()   { return slug; }
    public Plan getPlan()     { return plan; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
