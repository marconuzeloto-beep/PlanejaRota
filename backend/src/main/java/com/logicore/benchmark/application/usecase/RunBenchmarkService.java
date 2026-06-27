package com.logicore.benchmark.application.usecase;

import com.logicore.benchmark.domain.model.BenchmarkEntry;
import com.logicore.benchmark.domain.repository.BenchmarkRepository;
import com.logicore.core.domain.model.Customer;
import com.logicore.core.domain.model.DeliveryOrder;
import com.logicore.core.domain.model.Vehicle;
import com.logicore.core.domain.repository.CustomerRepository;
import com.logicore.core.domain.repository.OrderRepository;
import com.logicore.core.domain.repository.VehicleRepository;
import com.logicore.planning.domain.pipeline.PipelineContext;
import com.logicore.planning.domain.pipeline.PipelineResult;
import com.logicore.planning.domain.strategy.OptimizationStrategy;
import com.logicore.planning.domain.strategy.StrategyConfig;
import com.logicore.planning.domain.valueobject.PlanningOrder;
import com.logicore.planning.domain.valueobject.RouteConstraints;
import com.logicore.planning.infrastructure.pipeline.OptimizationPipeline;
import com.logicore.planning.infrastructure.strategy.StrategyRegistry;
import com.logicore.shared.domain.exception.ResourceNotFoundException;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Executa o Planning Engine com todas as estratégias registradas e persiste os resultados.
 * Usado para análise comparativa e ranking histórico de algoritmos.
 */
@Service
public class RunBenchmarkService {

    public record Command(
            UUID organizationId,
            UUID vehicleId,
            List<UUID> orderIds,
            GeoCoordinate depot,
            RouteConstraints constraints,
            List<String> strategyIdentifiers  // null = todas as estratégias
    ) {}

    public record Result(
            List<BenchmarkEntry> entries,
            String recommendedStrategy,
            double bestDistanceKm,
            long fastestStrategyMs
    ) {}

    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final StrategyRegistry strategyRegistry;
    private final OptimizationPipeline pipeline;
    private final BenchmarkRepository benchmarkRepository;

    public RunBenchmarkService(VehicleRepository vehicleRepository, OrderRepository orderRepository,
                                CustomerRepository customerRepository, StrategyRegistry strategyRegistry,
                                OptimizationPipeline pipeline, BenchmarkRepository benchmarkRepository) {
        this.vehicleRepository = vehicleRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.strategyRegistry = strategyRegistry;
        this.pipeline = pipeline;
        this.benchmarkRepository = benchmarkRepository;
    }

    @Transactional
    public Result execute(Command cmd) {
        Vehicle vehicle = vehicleRepository.findById(cmd.organizationId(), cmd.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));

        List<DeliveryOrder> orders = cmd.orderIds().stream()
                .map(id -> orderRepository.findById(cmd.organizationId(), id)
                        .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + id)))
                .toList();

        Map<UUID, Customer> customers = orders.stream()
                .map(DeliveryOrder::getCustomerId).distinct()
                .collect(Collectors.toMap(Function.identity(),
                        cid -> customerRepository.findById(cmd.organizationId(), cid)
                                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: " + cid))));

        List<PlanningOrder> planningOrders = orders.stream()
                .map(o -> PlanningOrder.from(o, customers.get(o.getCustomerId())))
                .toList();

        RouteConstraints constraints = cmd.constraints() != null
                ? cmd.constraints()
                : RouteConstraints.withDefaults(vehicle.getCapacity().kilograms());

        List<OptimizationStrategy> strategies = cmd.strategyIdentifiers() == null
                ? strategyRegistry.listAll()
                : cmd.strategyIdentifiers().stream().map(strategyRegistry::getByIdentifier).toList();

        List<BenchmarkEntry> entries = new ArrayList<>();
        for (OptimizationStrategy strategy : strategies) {
            try {
                long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                PipelineContext ctx = PipelineContext.of(cmd.organizationId(), planningOrders,
                        vehicle, cmd.depot(), constraints, strategy, StrategyConfig.defaults());
                PipelineResult result = pipeline.execute(ctx);
                long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

                BenchmarkEntry entry = BenchmarkEntry.from(cmd.organizationId(), strategy, result,
                        planningOrders.size(), Math.max(0, memAfter - memBefore));
                benchmarkRepository.save(entry);
                entries.add(entry);
            } catch (Exception e) {
                // Continua com próxima estratégia mesmo se uma falhar
            }
        }

        String best = entries.stream()
                .filter(BenchmarkEntry::feasible)
                .min(java.util.Comparator.comparingDouble(BenchmarkEntry::totalDistanceKm))
                .map(BenchmarkEntry::strategyIdentifier)
                .orElse(entries.isEmpty() ? "N/A" : entries.get(0).strategyIdentifier());

        double bestDist = entries.stream()
                .mapToDouble(BenchmarkEntry::totalDistanceKm).min().orElse(0);

        long fastest = entries.stream()
                .mapToLong(BenchmarkEntry::executionTimeMs).min().orElse(0);

        return new Result(entries, best, bestDist, fastest);
    }
}
