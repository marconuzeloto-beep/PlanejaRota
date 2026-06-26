package com.logicore.planning.domain.model;

import com.logicore.planning.domain.valueobject.DecisionStep;
import com.logicore.shared.domain.valueobject.GeoCoordinate;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Entidade filha de Route. Representa uma parada na rota planejada.
 * Carrega o DecisionStep do algoritmo — a razão explícita da posição desta parada.
 */
public class RouteStop {

    private final UUID id;
    private final UUID routeId;
    private final UUID orderId;
    private final UUID customerId;
    private final String customerName;
    private final GeoCoordinate location;
    private final int sequenceNumber;
    private final DecisionStep decisionStep;
    private StopStatus status;

    private RouteStop(UUID id, UUID routeId, UUID orderId, UUID customerId, String customerName,
                      GeoCoordinate location, int sequenceNumber, DecisionStep decisionStep,
                      StopStatus status) {
        this.id = id;
        this.routeId = routeId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.location = location;
        this.sequenceNumber = sequenceNumber;
        this.decisionStep = decisionStep;
        this.status = status;
    }

    public static RouteStop create(UUID routeId, DecisionStep step) {
        return new RouteStop(
                UUID.randomUUID(), routeId,
                step.orderId(), step.customerId(), step.customerName(),
                step.location(), step.position(), step, StopStatus.PENDING
        );
    }

    public static RouteStop reconstitute(UUID id, UUID routeId, UUID orderId, UUID customerId,
                                          String customerName, GeoCoordinate location,
                                          int sequenceNumber, DecisionStep decisionStep,
                                          StopStatus status) {
        return new RouteStop(id, routeId, orderId, customerId, customerName,
                location, sequenceNumber, decisionStep, status);
    }

    public void complete() {
        if (this.status != StopStatus.PENDING) throw new IllegalStateException("Parada não está pendente");
        this.status = StopStatus.COMPLETED;
    }

    public void fail() {
        if (this.status != StopStatus.PENDING) throw new IllegalStateException("Parada não está pendente");
        this.status = StopStatus.FAILED;
    }

    public UUID getId()             { return id; }
    public UUID getRouteId()        { return routeId; }
    public UUID getOrderId()        { return orderId; }
    public UUID getCustomerId()     { return customerId; }
    public String getCustomerName() { return customerName; }
    public GeoCoordinate getLocation() { return location; }
    public int getSequenceNumber()  { return sequenceNumber; }
    public DecisionStep getDecisionStep() { return decisionStep; }
    public StopStatus getStatus()   { return status; }

    public LocalTime getEstimatedArrivalTime() {
        return decisionStep != null ? decisionStep.estimatedArrivalTime() : null;
    }
}
