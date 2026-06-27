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
 * Validates that refrigerated cargo routes use a refrigerated vehicle (REFRIGERATED type).
 * Extend with per-order type requirements when PlanningOrder gains a requiredVehicleType field.
 */
@Component
public class VehicleTypeConstraint implements RouteConstraint {

    @Override
    public String getName() {
        return "VehicleTypeConstraint";
    }

    @Override
    public List<ConstraintViolation> evaluate(DecisionResult result, List<PlanningOrder> orders,
                                               Vehicle vehicle, RouteConstraints constraints) {
        // Refrigerated cargo requires a REFRIGERATED vehicle
        boolean routeHasRefrigerated = orders.stream()
                .anyMatch(o -> o.customerName() != null && o.customerName().toLowerCase().contains("[refrig]"));

        if (routeHasRefrigerated && vehicle.getType() != VehicleType.REFRIGERATED) {
            return List.of(new ConstraintViolation(
                    getName(),
                    ConstraintViolation.Severity.ERROR,
                    "Rota contém carga refrigerada mas veículo '%s' (tipo: %s) não é refrigerado."
                            .formatted(vehicle.getLicensePlate(), vehicle.getType()),
                    null, -1
            ));
        }
        return List.of();
    }
}
