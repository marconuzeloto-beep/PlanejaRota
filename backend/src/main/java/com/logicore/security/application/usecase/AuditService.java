package com.logicore.security.application.usecase;

import com.logicore.security.infrastructure.persistence.entity.AuditLogJpaEntity;
import com.logicore.security.infrastructure.persistence.repository.AuditLogJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogJpaRepository repository;

    public AuditService(AuditLogJpaRepository repository) {
        this.repository = repository;
    }

    public record AuditEntry(UUID id, UUID organizationId, UUID userId, String action,
                              String resourceType, String resourceId, String ipAddress,
                              String requestPath, String httpMethod, Integer httpStatus,
                              Long durationMs, Instant createdAt) {}

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(UUID organizationId, UUID userId, String action, String resourceType,
                    String resourceId, String ipAddress, String userAgent, String requestPath,
                    String httpMethod, Integer httpStatus, Long durationMs, String payloadSummary) {
        AuditLogJpaEntity entry = new AuditLogJpaEntity();
        entry.setOrganizationId(organizationId);
        entry.setUserId(userId);
        entry.setAction(action);
        entry.setResourceType(resourceType);
        entry.setResourceId(resourceId);
        entry.setIpAddress(ipAddress);
        entry.setUserAgent(userAgent);
        entry.setRequestPath(requestPath);
        entry.setHttpMethod(httpMethod);
        entry.setHttpStatus(httpStatus);
        entry.setDurationMs(durationMs);
        entry.setPayloadSummary(payloadSummary);
        entry.setCreatedAt(Instant.now());
        repository.save(entry);
    }

    @Transactional(readOnly = true)
    public Page<AuditEntry> list(UUID organizationId, UUID userId, Pageable pageable) {
        Page<AuditLogJpaEntity> page = userId != null
                ? repository.findByOrganizationIdAndUserIdOrderByCreatedAtDesc(organizationId, userId, pageable)
                : repository.findByOrganizationIdOrderByCreatedAtDesc(organizationId, pageable);
        return page.map(e -> new AuditEntry(
                e.getId(), e.getOrganizationId(), e.getUserId(), e.getAction(),
                e.getResourceType(), e.getResourceId(), e.getIpAddress(),
                e.getRequestPath(), e.getHttpMethod(), e.getHttpStatus(),
                e.getDurationMs(), e.getCreatedAt()
        ));
    }
}
