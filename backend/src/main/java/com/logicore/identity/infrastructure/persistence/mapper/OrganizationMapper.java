package com.logicore.identity.infrastructure.persistence.mapper;

import com.logicore.identity.domain.model.Organization;
import com.logicore.identity.infrastructure.persistence.entity.OrganizationJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {

    public Organization toDomain(OrganizationJpaEntity e) {
        return Organization.reconstitute(
                e.getId(), e.getName(), e.getSlug(),
                Organization.Plan.valueOf(e.getPlan()),
                Organization.Status.valueOf(e.getStatus()),
                e.getCreatedAt()
        );
    }

    public OrganizationJpaEntity toEntity(Organization org) {
        OrganizationJpaEntity e = new OrganizationJpaEntity();
        e.setId(org.getId());
        e.setName(org.getName());
        e.setSlug(org.getSlug());
        e.setPlan(org.getPlan().name());
        e.setStatus(org.getStatus().name());
        return e;
    }
}
