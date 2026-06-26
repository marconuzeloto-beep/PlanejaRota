package com.logicore.core.domain.model;

import com.logicore.shared.domain.exception.BusinessRuleException;
import com.logicore.shared.domain.valueobject.Money;
import com.logicore.shared.domain.valueobject.TimeWindow;
import com.logicore.shared.domain.valueobject.Weight;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class DeliveryOrderTest {

    private DeliveryOrder createOrder() {
        return DeliveryOrder.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ORD-001",
                "Produto X",
                new Weight(50.0),
                Money.brl(500.0),
                LocalDate.now().plusDays(1),
                new TimeWindow(LocalTime.of(8, 0), LocalTime.of(18, 0))
        );
    }

    @Test
    void createStartsAsPending() {
        DeliveryOrder order = createOrder();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getOrderCode()).isEqualTo("ORD-001");
    }

    @Test
    void assignTransitionsToAssigned() {
        DeliveryOrder order = createOrder();
        order.assign();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }

    @Test
    void assignThrowsWhenNotPending() {
        DeliveryOrder order = createOrder();
        order.assign();
        assertThatThrownBy(order::assign)
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void markDeliveredTransitions() {
        DeliveryOrder order = createOrder();
        order.assign();
        order.markDelivered();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void cancelThrowsWhenDelivered() {
        DeliveryOrder order = createOrder();
        order.assign();
        order.markDelivered();
        assertThatThrownBy(() -> order.cancel("cancelamento"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void cancelWorksWhenPending() {
        DeliveryOrder order = createOrder();
        order.cancel("mudança de planos");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void createWithPastDateThrows() {
        assertThatThrownBy(() -> DeliveryOrder.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ORD-002",
                "Produto Y",
                new Weight(10.0),
                Money.brl(100.0),
                LocalDate.now().minusDays(1),
                null
        )).isInstanceOf(BusinessRuleException.class);
    }
}
