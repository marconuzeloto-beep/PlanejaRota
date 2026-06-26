package com.logicore.identity.infrastructure.persistence.repository;

import com.logicore.identity.domain.model.User;
import com.logicore.identity.domain.repository.UserRepository;
import com.logicore.identity.infrastructure.persistence.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpa;
    private final UserMapper mapper;

    @Override
    public User save(User user) {
        return mapper.toDomain(jpa.save(mapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(UUID organizationId, UUID userId) {
        return jpa.findByOrganizationIdAndId(organizationId, userId).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(UUID organizationId, String email) {
        return jpa.findByOrganizationIdAndEmail(organizationId, email.toLowerCase()).map(mapper::toDomain);
    }

    @Override
    public List<User> findAll(UUID organizationId) {
        return jpa.findAllByOrganizationId(organizationId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByEmail(UUID organizationId, String email) {
        return jpa.existsByOrganizationIdAndEmail(organizationId, email.toLowerCase());
    }
}
