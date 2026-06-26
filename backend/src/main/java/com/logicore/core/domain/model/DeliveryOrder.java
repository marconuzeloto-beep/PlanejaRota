package com.logicore.core.domain.model;

import com.logicore.shared.domain.exception.BusinessRuleException;
import com.logicore.shared.domain.valueobject.Money;
import com.logicore.shared.domain.valueobject.TimeWindow;
import com.logicore.shared.domain.valueobject.Weight;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class DeliveryOrder {

    private final UUID id;
    private final UUID organizationId;
    private final UUID customerId;
    private final String orderCode;
    private final String description;
    private final Weight weight;
    private final Money declaredValue;
    private final LocalDate deliveryDate;
    private final TimeWindow deliveryWindow;
    private OrderStatus status;
    private final String notes;
    private final Instant createdAt;

    private DeliveryOrder(UUID id, UUID organizationId, UUID customerId, String orderCode,
                          String description, Weight weight, Money declaredValue,
                          LocalDate deliveryDate, TimeWindow deliveryWindow,
                          OrderStatus status, String notes, Instant createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.customerId = customerId;
        this.orderCode = orderCode;
        this.description = description;
        this.weight = weight;
        this.declaredValue = declaredValue;
        this.deliveryDate = deliveryDate;
        this.deliveryWindow = deliveryWindow;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public static DeliveryOrder create(UUID orgId, UUID customerId, String orderCode,
                                       String description, Weight weight, Money declaredValue,
                                       LocalDate deliveryDate, TimeWindow deliveryWindow) {
        if (orgId == null) throw new IllegalArgumentException("Organization ID é obrigatório");
        if (customerId == null) throw new IllegalArgumentException("Cliente é obrigatório");
        if (orderCode == null || orderCode.isBlank()) throw new IllegalArgumentException("Código do pedido é obrigatório");
        if (weight == null) throw new IllegalArgumentException("Peso é obrigatório");
        if (deliveryDate == null) throw new IllegalArgumentException("Data de entrega é obrigatória");
        if (deliveryDate.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Data de entrega não pode ser no passado: " + deliveryDate);
        }

        return new DeliveryOrder(UUID.randomUUID(), orgId, customerId, orderCode,
                description, weight, declaredValue, deliveryDate, deliveryWindow,
                OrderStatus.PENDING, null, Instant.now());
    }

    public static DeliveryOrder reconstitute(UUID id, UUID organizationId, UUID customerId,
                                             String orderCode, String description, Weight weight,
                                             Money declaredValue, LocalDate deliveryDate,
                                             TimeWindow deliveryWindow, OrderStatus status,
                                             String notes, Instant createdAt) {
        return new DeliveryOrder(id, organizationId, customerId, orderCode, description,
                weight, declaredValue, deliveryDate, deliveryWindow, status, notes, createdAt);
    }

    public void assign() {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessRuleException(
                    "Pedido não pode ser atribuído pois não está pendente. Status atual: " + this.status);
        }
        this.status = OrderStatus.ASSIGNED;
    }

    public void release() {
        this.status = OrderStatus.PENDING;
    }

    public void markDelivered() {
        this.status = OrderStatus.DELIVERED;
    }

    public void markFailed() {
        this.status = OrderStatus.FAILED;
    }

    public void cancel(String reason) {
        if (this.status == OrderStatus.DELIVERED) {
            throw new BusinessRuleException("Pedido já entregue não pode ser cancelado");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public UUID getId() { return id; }
    public UUID getOrganizationId() { return organizationId; }
    public UUID getCustomerId() { return customerId; }
    public String getOrderCode() { return orderCode; }
    public String getDescription() { return description; }
    public Weight getWeight() { return weight; }
    public Money getDeclaredValue() { return declaredValue; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public TimeWindow getDeliveryWindow() { return deliveryWindow; }
    public OrderStatus getStatus() { return status; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
}
