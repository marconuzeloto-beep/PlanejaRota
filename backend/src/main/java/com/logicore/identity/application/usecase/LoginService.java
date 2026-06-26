package com.logicore.identity.application.usecase;

import com.logicore.identity.domain.model.User;
import com.logicore.identity.domain.repository.OrganizationRepository;
import com.logicore.identity.domain.repository.UserRepository;
import com.logicore.identity.infrastructure.security.JwtService;
import com.logicore.shared.domain.exception.BusinessRuleException;
import com.logicore.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public record Command(String slug, String email, String password) {}

    public record Result(String accessToken, String refreshToken, UUID userId,
                         UUID organizationId, String role) {}

    @Transactional
    public Result execute(Command command) {
        var org = organizationRepository.findBySlug(command.slug())
                .orElseThrow(() -> new ResourceNotFoundException("Organização não encontrada"));

        if (!org.isActive())
            throw new BusinessRuleException("Organização inativa");

        // Mensagem genérica: não revela se e-mail existe ou não (segurança)
        User user = userRepository.findByEmail(org.getId(), command.email())
                .orElseThrow(() -> new BusinessRuleException("Credenciais inválidas"));

        if (!user.isActive())
            throw new BusinessRuleException("Credenciais inválidas");

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash()))
            throw new BusinessRuleException("Credenciais inválidas");

        user.recordLogin();
        userRepository.save(user);

        String access  = jwtService.generateToken(user.getId(), org.getId(), user.getRole().name());
        String refresh = jwtService.generateRefreshToken(user.getId());

        return new Result(access, refresh, user.getId(), org.getId(), user.getRole().name());
    }
}
