package com.logicore.planning.domain.valueobject;

import com.logicore.core.domain.model.Customer;
import com.logicore.core.domain.model.DeliveryOrder;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import com.logicore.shared.domain.valueobject.Priority;
import com.logicore.shared.domain.valueobject.TimeWindow;
import com.logicore.shared.domain.valueobject.Weight;

import java.util.UUID;

/**
 * Projeção planejável — une DeliveryOrder com os dados de Customer necessários ao algoritmo.
 * O domínio de Planning nunca busca Customer diretamente; recebe PlanningOrder já montado.
 */
public record PlanningOrder(
        UUID orderId,
        UUID customerId,
        String customerName,
        GeoCoordinate deliveryLocation,
        Weight weight,
        Priority priority,
        TimeWindow timeWindow
) {
    public static PlanningOrder from(DeliveryOrder order, Customer customer) {
        if (!order.getCustomerId().equals(customer.getId())) {
            throw new IllegalArgumentException("Order/Customer mismatch");
        }
        return new PlanningOrder(
                order.getId(),
                customer.getId(),
                customer.getName(),
                customer.getLocation(),
                order.getWeight(),
                customer.getPriority(),
                customer.getDeliveryWindow()
        );
    }
}
