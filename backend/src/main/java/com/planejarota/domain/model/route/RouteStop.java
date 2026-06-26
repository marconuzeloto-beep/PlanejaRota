package com.planejarota.domain.model.route;

import com.planejarota.domain.valueobject.GeoCoordinate;
import com.planejarota.domain.valueobject.TimeWindow;
import com.planejarota.domain.valueobject.Weight;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Entidade filha do aggregate Route.
 * Representa uma parada específica na rota — associada a um pedido de entrega.
 * Só pode ser criada e modificada através do aggregate root Route.
 */
public class RouteStop {

    public enum Status { PENDING, IN_TRANSIT, COMPLETED, FAILED, SKIPPED }

    private final UUID id;
    private final DeliveryOrder order;
    private int sequenceNumber;
    private Status status;
    private LocalTime estimatedArrival;
    private LocalTime actualArrival;
    private String failureReason;

    RouteStop(DeliveryOrder order, int sequenceNumber, LocalTime estimatedArrival) {
        if (order == null) throw new IllegalArgumentException("Pedido é obrigatório");
        this.id = UUID.randomUUID();
        this.order = order;
        this.sequenceNumber = sequenceNumber;
        this.status = Status.PENDING;
        this.estimatedArrival = estimatedArrival;
    }

    RouteStop reconstitute(UUID id, DeliveryOrder order, int sequenceNumber,
                           Status status, LocalTime estimatedArrival, LocalTime actualArrival, String failureReason) {
        RouteStop stop = new RouteStop(order, sequenceNumber, estimatedArrival);
        return stop;
    }

    void complete(LocalTime arrivalTime) {
        if (this.status != Status.IN_TRANSIT && this.status != Status.PENDING) {
            throw new IllegalStateException("Parada em status " + status + " não pode ser completada");
        }
        this.actualArrival = arrivalTime;
        this.status = Status.COMPLETED;
    }

    void fail(String reason) {
        this.status = Status.FAILED;
        this.failureReason = reason;
    }

    void updateSequence(int newSequence) {
        this.sequenceNumber = newSequence;
    }

    void updateEstimatedArrival(LocalTime time) {
        this.estimatedArrival = time;
    }

    public boolean isWithinTimeWindow() {
        TimeWindow window = order.getDeliveryWindow();
        if (window == null || estimatedArrival == null) return true;
        return window.contains(estimatedArrival);
    }

    public GeoCoordinate getLocation() { return order.getCustomer().getLocation(); }
    public Weight getWeight() { return order.getWeight(); }

    public UUID getId() { return id; }
    public DeliveryOrder getOrder() { return order; }
    public int getSequenceNumber() { return sequenceNumber; }
    public Status getStatus() { return status; }
    public LocalTime getEstimatedArrival() { return estimatedArrival; }
    public LocalTime getActualArrival() { return actualArrival; }
    public String getFailureReason() { return failureReason; }
}
