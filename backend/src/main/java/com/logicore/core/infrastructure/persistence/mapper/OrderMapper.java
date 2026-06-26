package com.logicore.core.infrastructure.persistence.mapper;

import com.logicore.core.domain.model.DeliveryOrder;
import com.logicore.core.domain.model.OrderStatus;
import com.logicore.core.infrastructure.persistence.entity.DeliveryOrderJpaEntity;
import com.logicore.shared.domain.valueobject.Money;
import com.logicore.shared.domain.valueobject.TimeWindow;
import com.logicore.shared.domain.valueobject.Weight;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderMapper {

    public DeliveryOrder toDomain(DeliveryOrderJpaEntity e) {
        Weight weight = new Weight(e.getWeightKg().doubleValue());

        Money declaredValue = (e.getDeclaredValue() != null)
                ? Money.brl(e.getDeclaredValue())
                : null;

        TimeWindow timeWindow = (e.getTwOpen() != null && e.getTwClose() != null)
                ? new TimeWindow(e.getTwOpen(), e.getTwClose())
                : null;

        OrderStatus status = OrderStatus.valueOf(e.getStatus());

        return DeliveryOrder.reconstitute(
                e.getId(),
                e.getOrganizationId(),
                e.getCustomerId(),
                e.getOrderCode(),
                e.getDescription(),
                weight,
                declaredValue,
                e.getDeliveryDate(),
                timeWindow,
                status,
                e.getNotes(),
                e.getCreatedAt()
        );
    }

    public DeliveryOrderJpaEntity toEntity(DeliveryOrder order) {
        DeliveryOrderJpaEntity e = new DeliveryOrderJpaEntity();
        e.setId(order.getId());
        e.setOrganizationId(order.getOrganizationId());
        e.setCustomerId(order.getCustomerId());
        e.setOrderCode(order.getOrderCode());
        e.setDescription(order.getDescription());
        e.setStatus(order.getStatus().name());
        e.setNotes(order.getNotes());
        e.setWeightKg(BigDecimal.valueOf(order.getWeight().kilograms()));

        if (order.getDeclaredValue() != null) {
            e.setDeclaredValue(order.getDeclaredValue().amount());
        }

        e.setDeliveryDate(order.getDeliveryDate());

        if (order.getDeliveryWindow() != null) {
            e.setTwOpen(order.getDeliveryWindow().openTime());
            e.setTwClose(order.getDeliveryWindow().closeTime());
        }

        e.setCreatedAt(order.getCreatedAt());

        return e;
    }
}
