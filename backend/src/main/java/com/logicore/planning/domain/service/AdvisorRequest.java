package com.logicore.planning.domain.service;

public record AdvisorRequest(
        int orderCount,
        boolean hasTimeWindows,
        boolean hasHazardousCargo,
        boolean hasMultipleVehicles,
        long slaMsLimit
) {}
