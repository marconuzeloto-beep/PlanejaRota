package com.logicore.planning.infrastructure.constraint;

import com.logicore.planning.domain.constraint.ConstraintViolation;
import com.logicore.planning.domain.constraint.RouteConstraint;
import com.logicore.planning.domain.valueobject.PlanningOrder;
import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.DecisionStep;
import com.logicore.planning.domain.valueobject.RouteConstraints;
import com.logicore.core.domain.model.Vehicle;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * EU regulation: driver must take a 45-minute break after 4.5h of continuous driving.
 * Emits a WARNING if total route duration exceeds 270 minutes without a planned break slot.
 */
@Component
public class DriverBreakConstraint implements RouteConstraint {

    private static final double MAX_CONTINUOUS_DRIVING_MIN = 270.0;

    @Override
    public String getName() {
        return "DriverBreakConstraint";
    }

    @Override
    public List<ConstraintViolation> evaluate(DecisionResult result, List<PlanningOrder> orders,
                                               Vehicle vehicle, RouteConstraints constraints) {
        if (!constraints.enforceDriverBreak()) {
            return List.of();
        }

        List<ConstraintViolation> violations = new ArrayList<>();
        double accumulatedMin = 0.0;
        int segmentStart = 0;

        double speedKmh = constraints.averageSpeedKmh();
        List<DecisionStep> steps = result.orderedSteps();
        for (int i = 0; i < steps.size(); i++) {
            double travelMin = (steps.get(i).distanceFromPreviousKm() / speedKmh) * 60.0
                               + constraints.stopDurationMinutes();
            accumulatedMin += travelMin;
            if (accumulatedMin > MAX_CONTINUOUS_DRIVING_MIN) {
                violations.add(new ConstraintViolation(
                        getName(),
                        ConstraintViolation.Severity.WARNING,
                        ("Segmento de paradas %d–%d excede %.0f minutos de condução contínua " +
                         "(%.0f min acumulados). Pausa obrigatória de 45min ausente.")
                                .formatted(segmentStart + 1, i + 1,
                                           MAX_CONTINUOUS_DRIVING_MIN, accumulatedMin),
                        null, i + 1
                ));
                accumulatedMin = 0.0;
                segmentStart = i + 1;
            }
        }
        return violations;
    }
}
