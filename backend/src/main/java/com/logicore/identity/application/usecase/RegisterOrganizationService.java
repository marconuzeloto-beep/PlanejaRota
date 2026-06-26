package com.logicore.identity.application.usecase;

import com.logicore.identity.domain.model.Organization;
import com.logicore.identity.domain.model.Role;
import com.logicore.identity.domain.model.User;
import com.logicore.identity.domain.repository.OrganizationRepository;
import com.logicore.identity.domain.repository.UserRepository;
import com.logicore.shared.domain.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterOrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public record Command(
            String organizationName,
            String slug,
            String adminName,
            String adminEmail,
            String adminPassword
    ) {}

    public record Result(UUID organizationId, UUID adminUserId) {}

    @Transactional
    public Result execute(Command command) {
        if (organizationRepository.existsBySlug(command.slug()))
            throw new BusinessRuleException("Slug já em uso: " + command.slug());

        Organization org = Organization.create(command.organizationName(), command.slug());
        org = organizationRepository.save(org);

        String hash = passwordEncoder.encode(command.adminPassword());
        User admin  = User.create(org.getId(), command.adminEmail(), hash, command.adminName(), Role.ADMIN);
        admin = userRepository.save(admin);

        return new Result(org.getId(), admin.getId());
    }
}
