package com.logicore.planning.infrastructure.constraint;

import com.logicore.core.domain.model.Vehicle;
import com.logicore.planning.domain.constraint.ConstraintViolation;
import com.logicore.planning.domain.constraint.RouteConstraint;
import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.PlanningOrder;
import com.logicore.planning.domain.valueobject.RouteConstraints;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VehicleCapacityConstraint implements RouteConstraint {

    @Override
    public String getName() { return "VEHICLE_CAPACITY"; }

    @Override
    public List<ConstraintViolation> evaluate(DecisionResult result, List<PlanningOrder> orders,
                                               Vehicle vehicle, RouteConstraints constraints) {
        double totalWeight = orders.stream().mapToDouble(o -> o.weight().kilograms()).sum();
        double capacity = vehicle.getCapacity().kilograms();

        if (totalWeight > capacity) {
            return List.of(ConstraintViolation.routeLevel(getName(), ConstraintViolation.Severity.ERROR,
                    String.format("Peso total %.1f kg excede capacidade do veículo %.1f kg (%.0f%%)",
                            totalWeight, capacity, (totalWeight / capacity) * 100)));
        }
        return List.of();
    }
}
