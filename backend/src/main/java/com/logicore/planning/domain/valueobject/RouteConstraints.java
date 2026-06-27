package com.logicore.planning.domain.valueobject;

/**
 * Encapsula os parâmetros de contexto para execução do algoritmo.
 * Vem das configurações da organização + tipo de veículo.
 */
public record RouteConstraints(
        double maxWeightKg,
        double averageSpeedKmh,
        int stopDurationMinutes,
        boolean hardTimeWindows,
        int maxStops,
        boolean enforceDriverBreak,
        boolean hasHazardousCargo
) {
    public RouteConstraints {
        if (maxWeightKg <= 0) throw new IllegalArgumentException("Capacidade máxima deve ser positiva");
        if (averageSpeedKmh <= 0) throw new IllegalArgumentException("Velocidade média deve ser positiva");
        if (stopDurationMinutes < 0) throw new IllegalArgumentException("Tempo de parada não pode ser negativo");
    }

    public static RouteConstraints withDefaults(double maxWeightKg) {
        return new RouteConstraints(maxWeightKg, 40.0, 10, false, 0, false, false);
    }

    public static RouteConstraints of(double maxWeightKg, double averageSpeedKmh,
                                      int stopDurationMinutes, boolean hardTimeWindows) {
        return new RouteConstraints(maxWeightKg, averageSpeedKmh, stopDurationMinutes, hardTimeWindows,
                0, false, false);
    }
}
