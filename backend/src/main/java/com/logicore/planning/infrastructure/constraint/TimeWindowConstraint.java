package com.logicore.planning.infrastructure.constraint;

import com.logicore.core.domain.model.Vehicle;
import com.logicore.planning.domain.constraint.ConstraintViolation;
import com.logicore.planning.domain.constraint.RouteConstraint;
import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.DecisionStep;
import com.logicore.planning.domain.valueobject.PlanningOrder;
import com.logicore.planning.domain.valueobject.RouteConstraints;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TimeWindowConstraint implements RouteConstraint {

    @Override
    public String getName() { return "TIME_WINDOW"; }

    @Override
    public List<ConstraintViolation> evaluate(DecisionResult result, List<PlanningOrder> orders,
                                               Vehicle vehicle, RouteConstraints constraints) {
        if (!constraints.hardTimeWindows()) return List.of();

        List<ConstraintViolation> violations = new ArrayList<>();
        for (DecisionStep step : result.orderedSteps()) {
            if (!step.withinTimeWindow()) {
                violations.add(ConstraintViolation.stopLevel(
                        getName(), ConstraintViolation.Severity.ERROR,
                        step.timeWindowWarning() != null ? step.timeWindowWarning()
                                : "Janela de tempo violada na parada " + step.position(),
                        step.orderId(), step.position()
                ));
            }
        }
        return violations;
    }
}
