package com.logicore.core.domain.repository;

import com.logicore.core.domain.model.DeliveryOrder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    DeliveryOrder save(DeliveryOrder order);
    Optional<DeliveryOrder> findById(UUID orgId, UUID orderId);
    List<DeliveryOrder> findPending(UUID orgId, LocalDate date);
    List<DeliveryOrder> findAll(UUID orgId);
    boolean existsByOrderCode(UUID orgId, String orderCode);
}
