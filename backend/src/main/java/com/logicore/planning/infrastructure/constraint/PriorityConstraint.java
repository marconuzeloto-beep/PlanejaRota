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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PriorityConstraint implements RouteConstraint {

    private static final int HIGH_PRIORITY_THRESHOLD = 4;
    private static final int HIGH_PRIORITY_MAX_POSITION_RATIO = 60; // % da rota

    @Override
    public String getName() { return "PRIORITY_ORDER"; }

    @Override
    public List<ConstraintViolation> evaluate(DecisionResult result, List<PlanningOrder> orders,
                                               Vehicle vehicle, RouteConstraints constraints) {
        if (orders.isEmpty()) return List.of();

        Map<UUID, PlanningOrder> orderMap = orders.stream()
                .collect(Collectors.toMap(PlanningOrder::orderId, o -> o));

        List<ConstraintViolation> violations = new ArrayList<>();
        int total = result.orderedSteps().size();

        for (DecisionStep step : result.orderedSteps()) {
            PlanningOrder order = orderMap.get(step.orderId());
            if (order == null) continue;
            int priority = order.priority().value();
            if (priority >= HIGH_PRIORITY_THRESHOLD) {
                int positionPct = (step.position() * 100) / total;
                if (positionPct > HIGH_PRIORITY_MAX_POSITION_RATIO) {
                    violations.add(ConstraintViolation.stopLevel(
                            getName(), ConstraintViolation.Severity.WARNING,
                            String.format("Cliente de prioridade %d visitado muito tarde (posição %d/%d)",
                                    priority, step.position(), total),
                            step.orderId(), step.position()
                    ));
                }
            }
        }
        return violations;
    }
}
