package com.logicore.simulation.application.usecase;

import com.logicore.core.domain.model.Customer;
import com.logicore.core.domain.model.DeliveryOrder;
import com.logicore.core.domain.model.Vehicle;
import com.logicore.core.domain.repository.CustomerRepository;
import com.logicore.core.domain.repository.OrderRepository;
import com.logicore.core.domain.repository.VehicleRepository;
import com.logicore.planning.domain.service.RouteBuilder;
import com.logicore.planning.domain.strategy.OptimizationStrategy;
import com.logicore.planning.domain.strategy.StrategyConfig;
import com.logicore.planning.domain.valueobject.DecisionResult;
import com.logicore.planning.domain.valueobject.PlanningOrder;
import com.logicore.planning.domain.valueobject.RouteConstraints;
import com.logicore.planning.infrastructure.strategy.StrategyRegistry;
import com.logicore.shared.domain.exception.ResourceNotFoundException;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import com.logicore.simulation.domain.valueobject.ScenarioComparison;
import com.logicore.simulation.domain.valueobject.ScenarioSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Executa o Planning Engine N vezes com estratégias diferentes e compara os resultados.
 * NUNCA persiste rotas — saída é sempre um ScenarioComparison em memória.
 */
@Service
public class CompareStrategiesService {

    public record Command(
            UUID organizationId,
            UUID vehicleId,
            List<UUID> orderIds,
            GeoCoordinate depot,
            RouteConstraints constraints,
            List<String> strategyIdentifiers  // quais estratégias simular; null = todas
    ) {}

    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final StrategyRegistry strategyRegistry;

    public CompareStrategiesService(VehicleRepository vehicleRepository,
                                     OrderRepository orderRepository,
                                     CustomerRepository customerRepository,
                                     StrategyRegistry strategyRegistry) {
        this.vehicleRepository = vehicleRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.strategyRegistry = strategyRegistry;
    }

    @Transactional(readOnly = true)
    public ScenarioComparison execute(Command cmd) {
        Vehicle vehicle = vehicleRepository.findById(cmd.organizationId(), cmd.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado: " + cmd.vehicleId()));

        List<DeliveryOrder> orders = cmd.orderIds().stream()
                .map(id -> orderRepository.findById(cmd.organizationId(), id)
                        .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + id)))
                .toList();

        Map<UUID, Customer> customers = orders.stream()
                .map(DeliveryOrder::getCustomerId)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        cid -> customerRepository.findById(cmd.organizationId(), cid)
                                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + cid))
                ));

        List<PlanningOrder> planningOrders = orders.stream()
                .map(o -> PlanningOrder.from(o, customers.get(o.getCustomerId())))
                .toList();

        RouteConstraints constraints = cmd.constraints() != null
                ? cmd.constraints()
                : RouteConstraints.withDefaults(vehicle.getCapacity().kilograms());

        List<OptimizationStrategy> strategies = cmd.strategyIdentifiers() == null
                ? strategyRegistry.listAll()
                : cmd.strategyIdentifiers().stream()
                        .map(strategyRegistry::getByIdentifier)
                        .toList();

        List<ScenarioSummary> summaries = strategies.stream()
                .map(strategy -> {
                    StrategyConfig config = StrategyConfig.defaults();
                    DecisionResult result = RouteBuilder.build(
                            planningOrders, vehicle, cmd.depot(), constraints, strategy, config);
                    return new ScenarioSummary(
                            strategy.getIdentifier(),
                            strategy.getType(),
                            strategy.getDescription(),
                            result.metrics(),
                            result
                    );
                })
                .toList();

        return ScenarioComparison.of(summaries);
    }
}
