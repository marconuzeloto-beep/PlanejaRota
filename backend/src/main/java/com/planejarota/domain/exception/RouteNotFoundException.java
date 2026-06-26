package com.planejarota.domain.exception;

import java.util.UUID;

public class RouteNotFoundException extends DomainException {
    public RouteNotFoundException(UUID routeId) {
        super("Rota não encontrada: " + routeId);
    }
}
