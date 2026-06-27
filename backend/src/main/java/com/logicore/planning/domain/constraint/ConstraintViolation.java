package com.logicore.planning.domain.constraint;

import java.util.UUID;

/**
 * Representa uma violação de restrição em uma parada específica ou na rota como um todo.
 */
public record ConstraintViolation(
        String constraintName,
        Severity severity,
        String message,
        UUID affectedOrderId,  // null = violação de rota (não de parada específica)
        int affectedStopPosition
) {
    public enum Severity { WARNING, ERROR }

    public static ConstraintViolation routeLevel(String constraint, Severity severity, String message) {
        return new ConstraintViolation(constraint, severity, message, null, -1);
    }

    public static ConstraintViolation stopLevel(String constraint, Severity severity,
                                                  String message, UUID orderId, int position) {
        return new ConstraintViolation(constraint, severity, message, orderId, position);
    }

    public boolean isError() { return severity == Severity.ERROR; }
}
