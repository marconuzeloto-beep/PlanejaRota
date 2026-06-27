package com.logicore.execution.api;

import com.logicore.execution.application.usecase.LiveExecutionService;
import com.logicore.shared.infrastructure.tenant.TenantContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/execution")
public class ExecutionController {

    private final LiveExecutionService executionService;

    public ExecutionController(LiveExecutionService executionService) {
        this.executionService = executionService;
    }

    public record StartRequest(
            @NotNull UUID vehicleId,
            UUID routeId,
            String driverName,
            int totalStops,
            Double plannedDistanceKm
    ) {}

    public record GpsUpdateRequest(@NotNull Double lat, @NotNull Double lng, Integer etaMinutes) {}

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LiveExecutionService.LiveExecutionDTO>> getActive() {
        return ResponseEntity.ok(executionService.getActive(TenantContext.get()));
    }

    @PostMapping("/start")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, UUID>> start(@Valid @RequestBody StartRequest req) {
        UUID orgId = TenantContext.get();
        UUID id = executionService.start(orgId, req.vehicleId(), req.routeId(),
                req.driverName(), req.totalStops(), req.plannedDistanceKm());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("executionId", id));
    }

    @PostMapping("/{executionId}/gps")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateGps(@PathVariable UUID executionId,
                                           @Valid @RequestBody GpsUpdateRequest req) {
        executionService.updateGps(TenantContext.get(), executionId,
                new LiveExecutionService.GpsUpdate(req.lat(), req.lng(), req.etaMinutes()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{executionId}/stop-complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> completeStop(@PathVariable UUID executionId) {
        executionService.completeStop(TenantContext.get(), executionId);
        return ResponseEntity.ok().build();
    }
}
