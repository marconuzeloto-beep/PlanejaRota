package com.planejarota.domain.model.route;

import com.planejarota.domain.valueobject.TimeWindow;
import com.planejarota.domain.valueobject.Weight;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Pedido de entrega — representa o que deve ser entregue, para quem, quando e quanto pesa.
 */
public class DeliveryOrder {

    public enum Status { PENDING, ASSIGNED, DELIVERED, FAILED, CANCELLED }

    private final UUID id;
    private final Customer customer;
    private String orderCode;
    private String description;
    private Weight weight;
    private BigDecimal value;
    private TimeWindow deliveryWindow;
    private LocalDate deliveryDate;
    private Status status;
    private String notes;

    private DeliveryOrder() {}

    public static DeliveryOrder create(
            Customer customer,
            String orderCode,
            String description,
            Weight weight,
            BigDecimal value,
            TimeWindow deliveryWindow,
            LocalDate deliveryDate
    ) {
        if (customer == null) throw new IllegalArgumentException("Cliente é obrigatório");
        if (orderCode == null || orderCode.isBlank()) throw new IllegalArgumentException("Código do pedido é obrigatório");
        if (weight == null) throw new IllegalArgumentException("Peso é obrigatório");
        if (deliveryDate == null) throw new IllegalArgumentException("Data de entrega é obrigatória");

        DeliveryOrder order = new DeliveryOrder();
        order.id = UUID.randomUUID();
        order.customer = customer;
        order.orderCode = orderCode;
        order.description = description;
        order.weight = weight;
        order.value = value;
        order.deliveryWindow = deliveryWindow;
        order.deliveryDate = deliveryDate;
        order.status = Status.PENDING;
        return order;
    }

    public void assign() {
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("Apenas pedidos pendentes podem ser atribuídos a uma rota");
        }
        this.status = Status.ASSIGNED;
    }

    public void cancel(String reason) {
        if (this.status == Status.DELIVERED) {
            throw new IllegalStateException("Pedido já entregue não pode ser cancelado");
        }
        this.status = Status.CANCELLED;
        this.notes = reason;
    }

    public UUID getId() { return id; }
    public Customer getCustomer() { return customer; }
    public String getOrderCode() { return orderCode; }
    public String getDescription() { return description; }
    public Weight getWeight() { return weight; }
    public BigDecimal getValue() { return value; }
    public TimeWindow getDeliveryWindow() { return deliveryWindow; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public Status getStatus() { return status; }
    public String getNotes() { return notes; }
}
