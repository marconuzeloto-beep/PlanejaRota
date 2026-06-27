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
public class MaximumDistanceConstraint implements RouteConstraint {

    private static final double DEFAULT_MAX_KM = 500.0;

    @Override
    public String getName() { return "MAXIMUM_DISTANCE"; }

    @Override
    public List<ConstraintViolation> evaluate(DecisionResult result, List<PlanningOrder> orders,
                                               Vehicle vehicle, RouteConstraints constraints) {
        double total = result.metrics().totalDistanceKm();
        double max = DEFAULT_MAX_KM;

        if (total > max) {
            return List.of(ConstraintViolation.routeLevel(getName(), ConstraintViolation.Severity.WARNING,
                    String.format("Distância total %.1f km supera limite recomendado de %.0f km", total, max)));
        }
        return List.of();
    }
}
