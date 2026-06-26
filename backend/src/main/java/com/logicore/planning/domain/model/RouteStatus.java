package com.logicore.planning.domain.model;

public enum RouteStatus {
    DRAFT,      // criada, ainda sem paradas confirmadas
    PLANNED,    // DecisionResult gerado, paradas confirmadas
    IN_PROGRESS,// execução iniciada
    COMPLETED,  // todas paradas concluídas
    CANCELLED   // cancelada antes de completar
}
