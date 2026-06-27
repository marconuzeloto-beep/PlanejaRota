package com.logicore.planning.domain.constraint;

import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.PlanningOrder;
import com.logicore.core.domain.model.Vehicle;
import com.logicore.planning.domain.valueobject.RouteConstraints;

import java.util.List;

/**
 * Plugin de validação de restrições.
 * O Planning Engine nunca conhece regras específicas — apenas consulta validadores registrados.
 */
public interface RouteConstraint {

    String getName();

    /**
     * Avalia o DecisionResult gerado por uma estratégia.
     * Retorna lista de violações (vazia = sem violações).
     */
    List<ConstraintViolation> evaluate(
            DecisionResult result,
            List<PlanningOrder> orders,
            Vehicle vehicle,
            RouteConstraints constraints
    );
}
