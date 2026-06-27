package com.logicore.planning.infrastructure.constraint;

import com.logicore.planning.domain.constraint.ConstraintViolation;
import com.logicore.planning.domain.constraint.RouteConstraint;
import com.logicore.planning.domain.valueobject.PlanningOrder;
import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.RouteConstraints;
import com.logicore.core.domain.model.Vehicle;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MaximumStopsConstraint implements RouteConstraint {

    private static final int DEFAULT_MAX_STOPS = 50;

    @Override
    public String getName() {
        return "MaximumStopsConstraint";
    }

    @Override
    public List<ConstraintViolation> evaluate(DecisionResult result, List<PlanningOrder> orders,
                                               Vehicle vehicle, RouteConstraints constraints) {
        int maxStops = constraints.maxStops() > 0 ? constraints.maxStops() : DEFAULT_MAX_STOPS;
        int actual = result.orderedSteps().size();

        if (actual > maxStops) {
            return List.of(new ConstraintViolation(
                    getName(),
                    ConstraintViolation.Severity.WARNING,
                    "Rota possui %d paradas, excedendo o máximo de %d configurado.".formatted(actual, maxStops),
                    null, actual
            ));
        }
        return List.of();
    }
}
