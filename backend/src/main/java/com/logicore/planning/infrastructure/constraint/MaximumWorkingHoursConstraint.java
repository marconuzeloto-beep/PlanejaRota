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
public class MaximumWorkingHoursConstraint implements RouteConstraint {

    private static final int MAX_WORKING_MINUTES = 10 * 60; // 10h

    @Override
    public String getName() { return "MAXIMUM_WORKING_HOURS"; }

    @Override
    public List<ConstraintViolation> evaluate(DecisionResult result, List<PlanningOrder> orders,
                                               Vehicle vehicle, RouteConstraints constraints) {
        int totalMinutes = result.metrics().totalEstimatedTimeMinutes();
        if (totalMinutes > MAX_WORKING_MINUTES) {
            return List.of(ConstraintViolation.routeLevel(getName(), ConstraintViolation.Severity.WARNING,
                    String.format("Tempo estimado %dh%02dm excede jornada máxima de 10h",
                            totalMinutes / 60, totalMinutes % 60)));
        }
        return List.of();
    }
}
