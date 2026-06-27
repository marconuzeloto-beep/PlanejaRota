package com.logicore.benchmark.api;

import com.logicore.benchmark.application.usecase.BenchmarkExportService;
import com.logicore.benchmark.application.usecase.BenchmarkStatsService;
import com.logicore.benchmark.application.usecase.RunBenchmarkService;
import com.logicore.benchmark.domain.model.BenchmarkEntry;
import com.logicore.benchmark.domain.repository.BenchmarkRepository;
import com.logicore.planning.domain.valueobject.RouteConstraints;
import com.logicore.shared.infrastructure.tenant.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Tag(name = "Benchmark", description = "Comparação histórica de algoritmos de otimização")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/benchmark")
public class BenchmarkController {

    private final RunBenchmarkService runBenchmarkService;
    private final BenchmarkRepository benchmarkRepository;
    private final BenchmarkExportService exportService;
    private final BenchmarkStatsService statsService;

    public BenchmarkController(RunBenchmarkService runBenchmarkService,
                                BenchmarkRepository benchmarkRepository,
                                BenchmarkExportService exportService,
                                BenchmarkStatsService statsService) {
        this.runBenchmarkService = runBenchmarkService;
        this.benchmarkRepository = benchmarkRepository;
        this.exportService = exportService;
        this.statsService = statsService;
    }

    public record RunBenchmarkRequest(
            @NotNull UUID vehicleId,
            @NotNull List<UUID> orderIds,
            @NotNull double depotLat,
            @NotNull double depotLng,
            List<String> strategyIdentifiers,
            Double maxWeightKg,
            Double avgSpeedKmh
    ) {}

    @Operation(summary = "Executar benchmark com todas as estratégias e persistir resultados")
    @PostMapping("/run")
    public RunBenchmarkService.Result run(@Valid @RequestBody RunBenchmarkRequest req) {
        UUID orgId = TenantContext.get();
        RouteConstraints constraints = req.maxWeightKg() != null || req.avgSpeedKmh() != null
                ? RouteConstraints.of(
                        req.maxWeightKg() != null ? req.maxWeightKg() : 1000.0,
                        req.avgSpeedKmh() != null ? req.avgSpeedKmh() : 40.0,
                        10, false)
                : null;
        return runBenchmarkService.execute(new RunBenchmarkService.Command(
                orgId, req.vehicleId(), req.orderIds(),
                new com.logicore.shared.domain.valueobject.GeoCoordinate(req.depotLat(), req.depotLng()),
                constraints, req.strategyIdentifiers()));
    }

    @Operation(summary = "Histórico de execuções de benchmark da organização")
    @GetMapping("/history")
    public List<BenchmarkEntry> history(@RequestParam(defaultValue = "50") int limit) {
        return benchmarkRepository.findByOrganization(TenantContext.get(), limit);
    }

    @Operation(summary = "Ranking agregado de estratégias com médias históricas")
    @GetMapping("/ranking")
    public List<BenchmarkRepository.BenchmarkSummary> ranking() {
        return benchmarkRepository.aggregateByStrategy(TenantContext.get());
    }

    @Operation(summary = "Histórico de uma estratégia específica")
    @GetMapping("/history/{strategyId}")
    public List<BenchmarkEntry> historyByStrategy(@PathVariable String strategyId,
                                                   @RequestParam(defaultValue = "20") int limit) {
        return benchmarkRepository.findByOrganizationAndStrategy(TenantContext.get(), strategyId, limit);
    }

    @Operation(summary = "Estatísticas avançadas por estratégia (p95, p99, stddev)")
    @GetMapping("/stats")
    public List<BenchmarkStatsService.PercentileStats> stats(
            @RequestParam(defaultValue = "500") int limit) {
        return statsService.computeStats(TenantContext.get(), limit);
    }

    @Operation(summary = "Exportar histórico em CSV")
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(@RequestParam(defaultValue = "500") int limit) throws IOException {
        var result = exportService.exportCsv(TenantContext.get(), limit);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .contentType(MediaType.parseMediaType(result.contentType()))
                .body(result.data());
    }

    @Operation(summary = "Exportar histórico em Excel (.xlsx)")
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(defaultValue = "500") int limit) throws IOException {
        var result = exportService.exportExcel(TenantContext.get(), limit);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .contentType(MediaType.parseMediaType(result.contentType()))
                .body(result.data());
    }
}
