package com.logicore.planning.infrastructure.constraint;

import com.logicore.planning.domain.constraint.ConstraintViolation;
import com.logicore.planning.domain.constraint.RouteConstraint;
import com.logicore.planning.domain.pipeline.PipelineContext;
import com.logicore.planning.domain.valueobject.DecisionResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Agrega todos os RouteConstraint registrados como Springs beans e os avalia em sequência.
 */
@Component
public class ConstraintEvaluatorService {

    private final List<RouteConstraint> constraints;

    public ConstraintEvaluatorService(List<RouteConstraint> constraints) {
        this.constraints = constraints;
    }

    public List<ConstraintViolation> evaluate(DecisionResult result, PipelineContext ctx) {
        List<ConstraintViolation> all = new ArrayList<>();
        for (RouteConstraint constraint : constraints) {
            all.addAll(constraint.evaluate(result, ctx.orders(), ctx.vehicle(), ctx.constraints()));
        }
        return all;
    }
}
