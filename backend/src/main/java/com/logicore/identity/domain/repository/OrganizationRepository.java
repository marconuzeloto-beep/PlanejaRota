package com.logicore.identity.domain.repository;

import com.logicore.identity.domain.model.Organization;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository {
    Organization save(Organization organization);
    Optional<Organization> findById(UUID id);
    Optional<Organization> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
