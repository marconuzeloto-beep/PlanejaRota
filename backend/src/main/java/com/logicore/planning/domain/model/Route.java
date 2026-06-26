package com.logicore.planning.domain.model;

import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.RouteConstraints;
import com.logicore.shared.domain.exception.BusinessRuleException;
import com.logicore.shared.domain.valueobject.GeoCoordinate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root do Planning Engine.
 * Detém a consistência entre paradas, capacidade do veículo e estado da rota.
 * DecisionResult é o único caminho de entrada para paradas — nunca addStop(stop) direto.
 */
public class Route {

    private final UUID id;
    private final UUID organizationId;
    private final UUID vehicleId;
    private final GeoCoordinate depot;
    private final LocalDate plannedDate;
    private final RouteConstraints constraints;
    private DecisionResult decisionResult;
    private RouteStatus status;
    private final List<RouteStop> stops;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int version;

    private Route(UUID id, UUID organizationId, UUID vehicleId, GeoCoordinate depot,
                  LocalDate plannedDate, RouteConstraints constraints, DecisionResult decisionResult,
                  RouteStatus status, List<RouteStop> stops, LocalDateTime createdAt,
                  LocalDateTime updatedAt, int version) {
        this.id = id;
        this.organizationId = organizationId;
        this.vehicleId = vehicleId;
        this.depot = depot;
        this.plannedDate = plannedDate;
        this.constraints = constraints;
        this.decisionResult = decisionResult;
        this.status = status;
        this.stops = new ArrayList<>(stops);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public static Route create(UUID organizationId, UUID vehicleId, GeoCoordinate depot,
                                LocalDate plannedDate, RouteConstraints constraints) {
        if (organizationId == null) throw new BusinessRuleException("OrganizationId obrigatório");
        if (vehicleId == null)      throw new BusinessRuleException("VehicleId obrigatório");
        if (depot == null)          throw new BusinessRuleException("Depot obrigatório");
        if (plannedDate == null)    throw new BusinessRuleException("Data planejada obrigatória");
        if (constraints == null)    throw new BusinessRuleException("Constraints obrigatórias");

        LocalDateTime now = LocalDateTime.now();
        return new Route(UUID.randomUUID(), organizationId, vehicleId, depot,
                plannedDate, constraints, null, RouteStatus.DRAFT,
                List.of(), now, now, 0);
    }

    public static Route reconstitute(UUID id, UUID organizationId, UUID vehicleId, GeoCoordinate depot,
                                      LocalDate plannedDate, RouteConstraints constraints,
                                      DecisionResult decisionResult, RouteStatus status,
                                      List<RouteStop> stops, LocalDateTime createdAt,
                                      LocalDateTime updatedAt, int version) {
        return new Route(id, organizationId, vehicleId, depot, plannedDate, constraints,
                decisionResult, status, stops, createdAt, updatedAt, version);
    }

    /**
     * Aplica um DecisionResult à rota, criando as RouteStops correspondentes.
     * Só pode ser chamado em estado DRAFT.
     */
    public void applyDecisionResult(DecisionResult result) {
        if (this.status != RouteStatus.DRAFT) {
            throw new BusinessRuleException("Rota só aceita DecisionResult em estado DRAFT");
        }
        if (result.metrics().totalWeightKg() > constraints.maxWeightKg()) {
            throw new BusinessRuleException(String.format(
                    "Peso total %.1f kg excede capacidade do veículo %.1f kg",
                    result.metrics().totalWeightKg(), constraints.maxWeightKg()));
        }

        this.stops.clear();
        result.orderedSteps().forEach(step -> this.stops.add(RouteStop.create(this.id, step)));
        this.decisionResult = result;
        this.status = RouteStatus.PLANNED;
        this.updatedAt = LocalDateTime.now();
    }

    public void startExecution() {
        if (this.status != RouteStatus.PLANNED) {
            throw new BusinessRuleException("Rota deve estar PLANNED para iniciar execução");
        }
        this.status = RouteStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        if (this.status != RouteStatus.IN_PROGRESS) {
            throw new BusinessRuleException("Rota deve estar IN_PROGRESS para ser completada");
        }
        this.status = RouteStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status == RouteStatus.COMPLETED || this.status == RouteStatus.CANCELLED) {
            throw new BusinessRuleException("Rota já finalizada não pode ser cancelada");
        }
        this.status = RouteStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public int stopCount()          { return stops.size(); }
    public boolean hasStops()       { return !stops.isEmpty(); }
    public UUID getId()             { return id; }
    public UUID getOrganizationId() { return organizationId; }
    public UUID getVehicleId()      { return vehicleId; }
    public GeoCoordinate getDepot() { return depot; }
    public LocalDate getPlannedDate()  { return plannedDate; }
    public RouteConstraints getConstraints() { return constraints; }
    public DecisionResult getDecisionResult() { return decisionResult; }
    public RouteStatus getStatus()  { return status; }
    public List<RouteStop> getStops() { return Collections.unmodifiableList(stops); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public int getVersion()         { return version; }
}
