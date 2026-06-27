package com.logicore.planning.application.usecase;

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
import com.logicore.planning.domain.valueobject.RouteConstraints;
import com.logicore.planning.infrastructure.strategy.StrategyRegistry;
import com.logicore.planning.domain.pipeline.PipelineContext;
import com.logicore.planning.infrastructure.pipeline.OptimizationPipeline;
import com.logicore.shared.domain.exception.BusinessRuleException;
import com.logicore.shared.domain.exception.ResourceNotFoundException;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import com.logicore.shared.infrastructure.metrics.PlanningMetrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PlanRouteService {

    public record Command(
            UUID organizationId,
            UUID vehicleId,
            List<UUID> orderIds,
            GeoCoordinate depot,
            LocalDate plannedDate,
            String strategyIdentifier,
            StrategyConfig strategyConfig,
            RouteConstraints constraints
    ) {}

    public record Result(UUID routeId, DecisionResult decisionResult) {}

    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RouteRepository routeRepository;
    private final StrategyRegistry strategyRegistry;
    private final OptimizationPipeline optimizationPipeline;
    private final PlanningMetrics metrics;

    public PlanRouteService(VehicleRepository vehicleRepository,
                             OrderRepository orderRepository,
                             CustomerRepository customerRepository,
                             RouteRepository routeRepository,
                             StrategyRegistry strategyRegistry,
                             OptimizationPipeline optimizationPipeline,
                             PlanningMetrics metrics) {
        this.vehicleRepository = vehicleRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.routeRepository = routeRepository;
        this.strategyRegistry = strategyRegistry;
        this.optimizationPipeline = optimizationPipeline;
        this.metrics = metrics;
    }

    @Transactional
    public Result execute(Command cmd) {
        long start = System.currentTimeMillis();
        Vehicle vehicle = vehicleRepository.findById(cmd.organizationId(), cmd.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado: " + cmd.vehicleId()));

        List<DeliveryOrder> orders = cmd.orderIds().stream()
                .map(id -> orderRepository.findById(cmd.organizationId(), id)
                        .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + id)))
                .toList();

        // Fetch customers for all orders and assemble PlanningOrders
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

        OptimizationStrategy strategy = strategyRegistry.getByIdentifier(
                cmd.strategyIdentifier() != null ? cmd.strategyIdentifier() : "SHORTEST_DISTANCE_V1");

        StrategyConfig config = cmd.strategyConfig() != null
                ? cmd.strategyConfig()
                : StrategyConfig.defaults();

        // Executa via pipeline: constraint eval + 2-opt + score
        PipelineContext ctx = PipelineContext.of(cmd.organizationId(), planningOrders,
                vehicle, cmd.depot(), constraints, strategy, config);
        var pipelineResult = optimizationPipeline.execute(ctx);
        DecisionResult decisionResult = pipelineResult.decisionResult();

        Route route = Route.create(cmd.organizationId(), cmd.vehicleId(),
                cmd.depot(), cmd.plannedDate(), constraints);
        route.applyDecisionResult(decisionResult);

        Route saved = routeRepository.save(route);
        metrics.recordPlanRoute(System.currentTimeMillis() - start);
        return new Result(saved.getId(), decisionResult);
    }
}
