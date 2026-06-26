package com.logicore.core.infrastructure.persistence.repository;

import com.logicore.core.domain.model.DeliveryOrder;
import com.logicore.core.domain.model.OrderStatus;
import com.logicore.core.domain.repository.OrderRepository;
import com.logicore.core.infrastructure.persistence.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderMapper mapper;

    @Override
    public DeliveryOrder save(DeliveryOrder order) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(order)));
    }

    @Override
    public Optional<DeliveryOrder> findById(UUID orgId, UUID orderId) {
        return jpaRepository.findByOrganizationIdAndId(orgId, orderId)
                .map(mapper::toDomain);
    }

    @Override
    public List<DeliveryOrder> findPending(UUID orgId, LocalDate date) {
        return jpaRepository.findByOrganizationIdAndDeliveryDateAndStatus(orgId, date, OrderStatus.PENDING.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<DeliveryOrder> findAll(UUID orgId) {
        return jpaRepository.findByOrganizationId(orgId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByOrderCode(UUID orgId, String orderCode) {
        return jpaRepository.existsByOrganizationIdAndOrderCode(orgId, orderCode);
    }
}
