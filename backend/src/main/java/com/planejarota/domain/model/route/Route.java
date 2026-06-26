package com.planejarota.domain.model.route;

import com.planejarota.domain.event.RouteCreatedEvent;
import com.planejarota.domain.event.RouteOptimizedEvent;
import com.planejarota.domain.exception.RouteCapacityExceededException;
import com.planejarota.domain.model.fleet.Vehicle;
import com.planejarota.domain.valueobject.GeoCoordinate;
import com.planejarota.domain.valueobject.Weight;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Aggregate Root do contexto de Route Planning.
 *
 * Route encapsula todas as regras de negócio relativas a uma rota de entrega:
 * - Validação de capacidade do veículo
 * - Ordenação de paradas
 * - Controle de estado (rascunho → planejada → em execução → concluída)
 * - Emissão de Domain Events
 *
 * As paradas (RouteStop) só existem dentro deste aggregate — nunca são
 * acessadas ou modificadas diretamente de fora.
 */
public class Route {

    public enum Status { DRAFT, PLANNED, IN_PROGRESS, COMPLETED, CANCELLED }

    private UUID id;
    private String name;
    private Vehicle vehicle;
    private GeoCoordinate depotLocation;
    private LocalDate scheduledDate;
    private LocalTime departureTime;
    private Status status;
    private final List<RouteStop> stops = new ArrayList<>();
    private final List<Object> domainEvents = new ArrayList<>();
    private double totalDistanceKm;

    private Route() {}

    public static Route create(
            String name,
            Vehicle vehicle,
            GeoCoordinate depotLocation,
            LocalDate scheduledDate,
            LocalTime departureTime
    ) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Nome da rota é obrigatório");
        if (vehicle == null) throw new IllegalArgumentException("Veículo é obrigatório");
        if (depotLocation == null) throw new IllegalArgumentException("Local de partida é obrigatório");
        if (scheduledDate == null) throw new IllegalArgumentException("Data de execução é obrigatória");

        Route route = new Route();
        route.id = UUID.randomUUID();
        route.name = name;
        route.vehicle = vehicle;
        route.depotLocation = depotLocation;
        route.scheduledDate = scheduledDate;
        route.departureTime = departureTime != null ? departureTime : LocalTime.of(8, 0);
        route.status = Status.DRAFT;

        route.domainEvents.add(new RouteCreatedEvent(route.id, route.name, route.scheduledDate));
        return route;
    }

    /**
     * Adiciona um pedido à rota, verificando capacidade do veículo antes.
     * Usa a regra de negócio: peso total não pode exceder capacidade do veículo.
     */
    public RouteStop addStop(DeliveryOrder order) {
        ensureModifiable();

        Weight projected = currentTotalWeight().add(order.getWeight());
        if (projected.exceeds(vehicle.getCapacity())) {
            throw new RouteCapacityExceededException(
                projected.kilograms(), vehicle.getCapacity().kilograms()
            );
        }

        order.assign();
        RouteStop stop = new RouteStop(order, stops.size() + 1, null);
        stops.add(stop);
        return stop;
    }

    /**
     * Remove uma parada da rota, resequenciando automaticamente.
     */
    public void removeStop(UUID stopId) {
        ensureModifiable();
        stops.removeIf(s -> s.getId().equals(stopId));
        resequence();
    }

    /**
     * Reordena as paradas conforme nova sequência fornecida.
     * O algoritmo de otimização chama este método após calcular a ordem ótima.
     */
    public void reorderStops(List<UUID> orderedStopIds) {
        ensureModifiable();
        Map<UUID, RouteStop> stopMap = new HashMap<>();
        stops.forEach(s -> stopMap.put(s.getId(), s));

        if (orderedStopIds.size() != stops.size()) {
            throw new IllegalArgumentException("Lista de IDs deve conter todos os IDs de paradas da rota");
        }

        stops.clear();
        int seq = 1;
        for (UUID stopId : orderedStopIds) {
            RouteStop stop = stopMap.get(stopId);
            if (stop == null) throw new IllegalArgumentException("Stop ID não pertence a esta rota: " + stopId);
            stop.updateSequence(seq++);
            stops.add(stop);
        }

        domainEvents.add(new RouteOptimizedEvent(this.id, calculateTotalDistance()));
    }

    public void plan() {
        if (status != Status.DRAFT) throw new IllegalStateException("Apenas rotas em rascunho podem ser planejadas");
        if (stops.isEmpty()) throw new IllegalStateException("Rota precisa ter ao menos uma parada");
        this.status = Status.PLANNED;
    }

    public void startExecution() {
        if (status != Status.PLANNED) throw new IllegalStateException("Apenas rotas planejadas podem ser iniciadas");
        this.status = Status.IN_PROGRESS;
    }

    public void completeStop(UUID stopId, LocalTime arrivalTime) {
        RouteStop stop = findStop(stopId);
        stop.complete(arrivalTime);
        if (stops.stream().allMatch(s -> s.getStatus() == RouteStop.Status.COMPLETED
                || s.getStatus() == RouteStop.Status.SKIPPED
                || s.getStatus() == RouteStop.Status.FAILED)) {
            this.status = Status.COMPLETED;
        }
    }

    public void cancel(String reason) {
        if (status == Status.COMPLETED) throw new IllegalStateException("Rota concluída não pode ser cancelada");
        this.status = Status.CANCELLED;
    }

    public double calculateTotalDistance() {
        if (stops.isEmpty()) return 0;
        double total = 0;
        GeoCoordinate current = depotLocation;
        for (RouteStop stop : stops) {
            total += current.distanceInKmTo(stop.getLocation());
            current = stop.getLocation();
        }
        total += current.distanceInKmTo(depotLocation);
        this.totalDistanceKm = total;
        return total;
    }

    private Weight currentTotalWeight() {
        return stops.stream()
                .map(RouteStop::getWeight)
                .reduce(Weight.zero(), Weight::add);
    }

    private void ensureModifiable() {
        if (status != Status.DRAFT) {
            throw new IllegalStateException("Rota em status " + status + " não pode ser modificada");
        }
    }

    private void resequence() {
        for (int i = 0; i < stops.size(); i++) {
            stops.get(i).updateSequence(i + 1);
        }
    }

    private RouteStop findStop(UUID stopId) {
        return stops.stream()
                .filter(s -> s.getId().equals(stopId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Parada não encontrada: " + stopId));
    }

    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public Vehicle getVehicle() { return vehicle; }
    public GeoCoordinate getDepotLocation() { return depotLocation; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public LocalTime getDepartureTime() { return departureTime; }
    public Status getStatus() { return status; }
    public List<RouteStop> getStops() { return Collections.unmodifiableList(stops); }
    public double getTotalDistanceKm() { return totalDistanceKm; }
}
