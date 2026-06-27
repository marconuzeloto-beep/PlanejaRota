package com.logicore.planning.infrastructure.constraint;

import com.logicore.planning.domain.constraint.ConstraintViolation;
import com.logicore.planning.domain.constraint.RouteConstraint;
import com.logicore.planning.domain.valueobject.PlanningOrder;
import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.RouteConstraints;
import com.logicore.core.domain.model.Vehicle;
import com.logicore.core.domain.model.VehicleType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates that hazardous cargo is only assigned to ADR-certified vehicle types.
 * VehicleType.TRUCK_ADR signals ADR certification in the current domain model.
 */
@Component
public class HazardousCargoConstraint implements RouteConstraint {

    @Override
    public String getName() {
        return "HazardousCargoConstraint";
    }

    @Override
    public List<ConstraintViolation> evaluate(DecisionResult result, List<PlanningOrder> orders,
                                               Vehicle vehicle, RouteConstraints constraints) {
        if (!constraints.hasHazardousCargo()) {
            return List.of();
        }

        boolean adrCapable = vehicle.getType() == VehicleType.TRUCK_ADR;
        if (adrCapable) {
            return List.of();
        }

        return List.of(new ConstraintViolation(
                getName(),
                ConstraintViolation.Severity.ERROR,
                "Rota contém carga perigosa (flag hasHazardousCargo=true) mas veículo '%s' (tipo: %s) não possui certificação ADR."
                        .formatted(vehicle.getLicensePlate(), vehicle.getType()),
                null, -1
        ));
    }
}
