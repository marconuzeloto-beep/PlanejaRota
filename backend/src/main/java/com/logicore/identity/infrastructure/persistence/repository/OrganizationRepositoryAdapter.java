package com.logicore.identity.infrastructure.persistence.repository;

import com.logicore.identity.domain.model.Organization;
import com.logicore.identity.domain.repository.OrganizationRepository;
import com.logicore.identity.infrastructure.persistence.mapper.OrganizationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrganizationRepositoryAdapter implements OrganizationRepository {

    private final OrganizationJpaRepository jpa;
    private final OrganizationMapper mapper;

    @Override
    public Organization save(Organization org) {
        return mapper.toDomain(jpa.save(mapper.toEntity(org)));
    }

    @Override
    public Optional<Organization> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Organization> findBySlug(String slug) {
        return jpa.findBySlug(slug).map(mapper::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpa.existsBySlug(slug);
    }
}
