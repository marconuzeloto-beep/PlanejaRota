package com.logicore.api.dto.response;

import java.time.LocalTime;
import java.util.UUID;

public record MapStopDTO(
        int position,
        UUID orderId,
        UUID customerId,
        String customerName,
        double lat,
        double lng,
        double distanceFromPreviousKm,
        double cumulativeDistanceKm,
        LocalTime estimatedArrivalTime,
        boolean withinTimeWindow,
        String timeWindowWarning,
        String decisionReasonType,
        String decisionReasonDetail
) {}
