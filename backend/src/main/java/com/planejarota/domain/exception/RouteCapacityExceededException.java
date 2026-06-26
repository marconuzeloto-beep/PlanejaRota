package com.planejarota.domain.exception;

public class RouteCapacityExceededException extends DomainException {
    public RouteCapacityExceededException(double current, double max) {
        super(String.format("Capacidade do veículo excedida: %.1f kg / %.1f kg máximo", current, max));
    }
}
