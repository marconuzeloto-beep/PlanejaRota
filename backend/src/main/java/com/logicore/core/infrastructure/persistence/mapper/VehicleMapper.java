package com.logicore.core.infrastructure.persistence.mapper;

import com.logicore.core.domain.model.Vehicle;
import com.logicore.core.domain.model.VehicleStatus;
import com.logicore.core.domain.model.VehicleType;
import com.logicore.core.infrastructure.persistence.entity.VehicleJpaEntity;
import com.logicore.shared.domain.valueobject.Money;
import com.logicore.shared.domain.valueobject.Weight;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class VehicleMapper {

    public Vehicle toDomain(VehicleJpaEntity e) {
        Weight capacity = new Weight(e.getCapacityKg().doubleValue());

        Money costPerKm = (e.getCostPerKm() != null)
                ? Money.brl(e.getCostPerKm())
                : null;

        VehicleType type = VehicleType.valueOf(e.getType());
        VehicleStatus status = VehicleStatus.valueOf(e.getStatus());

        return Vehicle.reconstitute(
                e.getId(),
                e.getOrganizationId(),
                e.getLicensePlate(),
                e.getModel(),
                type,
                capacity,
                costPerKm,
                status,
                e.getNotes(),
                e.getCreatedAt()
        );
    }

    public VehicleJpaEntity toEntity(Vehicle vehicle) {
        VehicleJpaEntity e = new VehicleJpaEntity();
        e.setId(vehicle.getId());
        e.setOrganizationId(vehicle.getOrganizationId());
        e.setLicensePlate(vehicle.getLicensePlate());
        e.setModel(vehicle.getModel());
        e.setType(vehicle.getType().name());
        e.setStatus(vehicle.getStatus().name());
        e.setCapacityKg(BigDecimal.valueOf(vehicle.getCapacity().kilograms()));

        if (vehicle.getCostPerKm() != null) {
            e.setCostPerKm(vehicle.getCostPerKm().amount());
        }

        e.setNotes(vehicle.getNotes());
        e.setCreatedAt(vehicle.getCreatedAt());

        return e;
    }
}
