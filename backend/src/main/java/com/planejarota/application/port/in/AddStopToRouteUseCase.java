package com.planejarota.application.port.in;

import java.util.UUID;

public interface AddStopToRouteUseCase {

    record Command(UUID routeId, UUID orderId) {}

    UUID execute(Command command);
}
