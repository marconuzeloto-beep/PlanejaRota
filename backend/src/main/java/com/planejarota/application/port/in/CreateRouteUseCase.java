package com.planejarota.application.port.in;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Porta de entrada para criação de rotas.
 * Define o contrato que os controllers usam — isola a camada de interface
 * da implementação do use case.
 */
public interface CreateRouteUseCase {

    record Command(
            String name,
            UUID vehicleId,
            double depotLatitude,
            double depotLongitude,
            LocalDate scheduledDate,
            LocalTime departureTime
    ) {}

    UUID execute(Command command);
}
