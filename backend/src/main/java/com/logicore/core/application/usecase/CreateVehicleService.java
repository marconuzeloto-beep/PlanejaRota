package com.logicore.core.application.usecase;

import com.logicore.core.domain.model.Vehicle;
import com.logicore.core.domain.model.VehicleType;
import com.logicore.core.domain.repository.VehicleRepository;
import com.logicore.shared.domain.valueobject.Money;
import com.logicore.shared.domain.valueobject.Weight;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateVehicleService {

    private final VehicleRepository vehicleRepository;

    public record Command(
            UUID orgId,
            String licensePlate,
            String model,
            VehicleType type,
            double capacityKg,
            BigDecimal costPerKm
    ) {}

    public UUID execute(Command command) {
        Money costPerKmMoney = (command.costPerKm() != null)
                ? Money.brl(command.costPerKm())
                : null;

        Vehicle vehicle = Vehicle.create(
                command.orgId(),
                command.licensePlate(),
                command.model(),
                command.type(),
                new Weight(command.capacityKg()),
                costPerKmMoney
        );

        Vehicle saved = vehicleRepository.save(vehicle);
        return saved.getId();
    }
}
