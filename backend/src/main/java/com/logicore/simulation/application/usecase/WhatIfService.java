package com.logicore.simulation.application.usecase;

import com.logicore.core.domain.model.Customer;
import com.logicore.core.domain.model.DeliveryOrder;
import com.logicore.core.domain.model.Vehicle;
import com.logicore.core.domain.repository.CustomerRepository;
import com.logicore.core.domain.repository.OrderRepository;
import com.logicore.core.domain.repository.VehicleRepository;
import com.logicore.planning.domain.model.Route;
import com.logicore.planning.domain.repository.RouteRepository;
import com.logicore.planning.domain.service.RouteBuilder;
import com.logicore.planning.domain.strategy.OptimizationStrategy;
import com.logicore.planning.domain.strategy.StrategyConfig;
import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.PlanningOrder;
import com.logicore.planning.infrastructure.strategy.StrategyRegistry;
import com.logicore.shared.domain.exception.ResourceNotFoundException;
import com.logicore.simulation.domain.valueobject.ImpactReport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Simula o impacto de adicionar um pedido extra a uma rota já planejada.
 * NUNCA modifica a rota original — pura simulação em memória.
 */
@Service
public class WhatIfService {

    public record Command(
            UUID organizationId,
            UUID routeId,
            UUID newOrderId
    ) {}

    private final RouteRepository routeRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final StrategyRegistry strategyRegistry;

    public WhatIfService(RouteRepository routeRepository,
                          OrderRepository orderRepository,
                          CustomerRepository customerRepository,
                          VehicleRepository vehicleRepository,
                          StrategyRegistry strategyRegistry) {
        this.routeRepository = routeRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.strategyRegistry = strategyRegistry;
    }

    @Transactional(readOnly = true)
    public ImpactReport execute(Command cmd) {
        Route route = routeRepository.findById(cmd.organizationId(), cmd.routeId())
                .orElseThrow(() -> new ResourceNotFoundException("Rota não encontrada: " + cmd.routeId()));

        DecisionResult original = route.getDecisionResult();
        if (original == null) throw new IllegalStateException("Rota ainda não foi planejada");

        Vehicle vehicle = vehicleRepository.findById(cmd.organizationId(), route.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));

        DeliveryOrder newOrder = orderRepository.findById(cmd.organizationId(), cmd.newOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + cmd.newOrderId()));

        Customer newCustomer = customerRepository.findById(cmd.organizationId(), newOrder.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

        // Rebuild existing PlanningOrders from the DecisionResult steps
        List<PlanningOrder> existingOrders = original.orderedSteps().stream()
                .map(step -> new PlanningOrder(
                        step.orderId(), step.customerId(), step.customerName(),
                        step.location(), null, null, null))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        // We need proper PlanningOrders with weight/priority — re-fetch from orders
        // For what-if we use the same strategy that generated the original result
        OptimizationStrategy strategy = strategyRegistry.getByType(original.strategyType());
        StrategyConfig config = original.strategyConfig() != null
                ? original.strategyConfig() : StrategyConfig.defaults();

        // Fetch all original orders from the route's decision steps
        List<PlanningOrder> originalPlanningOrders = original.orderedSteps().stream()
                .map(step -> {
                    DeliveryOrder o = orderRepository.findById(cmd.organizationId(), step.orderId())
                            .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + step.orderId()));
                    Customer c = customerRepository.findById(cmd.organizationId(), o.getCustomerId())
                            .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
                    return PlanningOrder.from(o, c);
                })
                .toList();

        List<PlanningOrder> withNewOrder = new ArrayList<>(originalPlanningOrders);
        withNewOrder.add(PlanningOrder.from(newOrder, newCustomer));

        DecisionResult updated = RouteBuilder.build(
                withNewOrder, vehicle, route.getDepot(), route.getConstraints(), strategy, config);

        return ImpactReport.of(cmd.newOrderId(), original, updated);
    }
}
