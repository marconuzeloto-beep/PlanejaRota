package com.logicore.identity.infrastructure.persistence.mapper;

import com.logicore.identity.domain.model.Role;
import com.logicore.identity.domain.model.User;
import com.logicore.identity.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserJpaEntity e) {
        return User.reconstitute(
                e.getId(), e.getOrganizationId(), e.getEmail(),
                e.getPasswordHash(), e.getFullName(),
                Role.valueOf(e.getRole()), e.isActive(),
                e.getLastLoginAt(), e.getCreatedAt()
        );
    }

    public UserJpaEntity toEntity(User user) {
        UserJpaEntity e = new UserJpaEntity();
        e.setId(user.getId());
        e.setOrganizationId(user.getOrganizationId());
        e.setEmail(user.getEmail());
        e.setPasswordHash(user.getPasswordHash());
        e.setFullName(user.getFullName());
        e.setRole(user.getRole().name());
        e.setActive(user.isActive());
        e.setLastLoginAt(user.getLastLoginAt());
        return e;
    }
}
