package com.logicore.analytics.domain.model;

import java.util.List;

public record AnalyticsSummary(
        int totalRoutes,
        int completedRoutes,
        double totalDistanceKm,
        double avgDistanceKmPerRoute,
        double avgRouteScore,
        int totalOrdersPlanned,
        int totalOrdersDelivered,
        double deliverySuccessRate,
        double onTimeRate,
        double totalFuelConsumptionL,
        double totalCo2Kg,
        int totalConstraintViolations,
        List<DailySnapshot> dailyBreakdown
) {}
