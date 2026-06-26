package com.logicore.identity.domain.repository;

import com.logicore.identity.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID organizationId, UUID userId);
    Optional<User> findByEmail(UUID organizationId, String email);
    List<User> findAll(UUID organizationId);
    boolean existsByEmail(UUID organizationId, String email);
}
