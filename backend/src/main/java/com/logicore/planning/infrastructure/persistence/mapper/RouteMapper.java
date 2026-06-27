package com.logicore.planning.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logicore.planning.domain.model.Route;
import com.logicore.planning.domain.model.RouteStatus;
import com.logicore.planning.domain.model.RouteStop;
import com.logicore.planning.domain.model.StopStatus;
import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.DecisionStep;
import com.logicore.planning.domain.valueobject.RouteConstraints;
import com.logicore.planning.infrastructure.persistence.entity.RouteJpaEntity;
import com.logicore.planning.infrastructure.persistence.entity.RouteStopJpaEntity;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteMapper {

    private final ObjectMapper objectMapper;

    public RouteMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RouteJpaEntity toEntity(Route route) {
        RouteJpaEntity e = new RouteJpaEntity();
        e.setId(route.getId());
        e.setOrganizationId(route.getOrganizationId());
        e.setVehicleId(route.getVehicleId());
        e.setDepotLat(route.getDepot().latitude());
        e.setDepotLng(route.getDepot().longitude());
        e.setPlannedDate(route.getPlannedDate());
        e.setStatus(route.getStatus().name());
        e.setMaxWeightKg(route.getConstraints().maxWeightKg());
        e.setAvgSpeedKmh(route.getConstraints().averageSpeedKmh());
        e.setStopDurationMinutes(route.getConstraints().stopDurationMinutes());
        e.setHardTimeWindows(route.getConstraints().hardTimeWindows());
        e.setDecisionResultJson(toJson(route.getDecisionResult()));
        e.setCreatedAt(route.getCreatedAt());
        e.setUpdatedAt(route.getUpdatedAt());
        e.setVersion(route.getVersion());

        List<RouteStopJpaEntity> stopEntities = route.getStops().stream()
                .map(stop -> toStopEntity(stop, e))
                .toList();
        e.setStops(stopEntities);
        return e;
    }

    public Route toDomain(RouteJpaEntity e) {
        GeoCoordinate depot = new GeoCoordinate(e.getDepotLat(), e.getDepotLng());
        RouteConstraints constraints = RouteConstraints.of(
                e.getMaxWeightKg(), e.getAvgSpeedKmh(),
                e.getStopDurationMinutes(), e.getHardTimeWindows());
        DecisionResult decisionResult = fromJson(e.getDecisionResultJson(), DecisionResult.class);

        List<RouteStop> stops = e.getStops().stream()
                .map(this::toStopDomain)
                .toList();

        return Route.reconstitute(
                e.getId(), e.getOrganizationId(), e.getVehicleId(),
                depot, e.getPlannedDate(), constraints, decisionResult,
                RouteStatus.valueOf(e.getStatus()), stops,
                e.getCreatedAt(), e.getUpdatedAt(), e.getVersion());
    }

    private RouteStopJpaEntity toStopEntity(RouteStop stop, RouteJpaEntity route) {
        RouteStopJpaEntity e = new RouteStopJpaEntity();
        e.setId(stop.getId());
        e.setRoute(route);
        e.setOrderId(stop.getOrderId());
        e.setCustomerId(stop.getCustomerId());
        e.setCustomerName(stop.getCustomerName());
        e.setLocationLat(stop.getLocation().latitude());
        e.setLocationLng(stop.getLocation().longitude());
        e.setSequenceNumber(stop.getSequenceNumber());
        e.setStatus(stop.getStatus().name());
        e.setEstimatedArrivalTime(stop.getEstimatedArrivalTime());
        e.setDecisionStepJson(toJson(stop.getDecisionStep()));
        return e;
    }

    private RouteStop toStopDomain(RouteStopJpaEntity e) {
        GeoCoordinate location = new GeoCoordinate(e.getLocationLat(), e.getLocationLng());
        DecisionStep step = fromJson(e.getDecisionStepJson(), DecisionStep.class);
        return RouteStop.reconstitute(
                e.getId(), e.getRoute().getId(), e.getOrderId(), e.getCustomerId(),
                e.getCustomerName(), location, e.getSequenceNumber(), step,
                StopStatus.valueOf(e.getStatus()));
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Falha ao serializar para JSON: " + obj.getClass().getSimpleName(), ex);
        }
    }

    private <T> T fromJson(String json, Class<T> type) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Falha ao desserializar JSON para " + type.getSimpleName(), ex);
        }
    }
}
