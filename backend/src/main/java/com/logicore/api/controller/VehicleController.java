package com.logicore.api.controller;

import com.logicore.api.dto.request.CreateVehicleRequest;
import com.logicore.core.application.usecase.CreateVehicleService;
import com.logicore.core.domain.model.Vehicle;
import com.logicore.core.domain.model.VehicleType;
import com.logicore.core.domain.repository.VehicleRepository;
import com.logicore.shared.infrastructure.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final CreateVehicleService createVehicleService;
    private final VehicleRepository vehicleRepository;

    public VehicleController(CreateVehicleService createVehicleService,
                              VehicleRepository vehicleRepository) {
        this.createVehicleService = createVehicleService;
        this.vehicleRepository = vehicleRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> create(@Valid @RequestBody CreateVehicleRequest req) {
        UUID orgId = TenantContext.get();
        UUID id = createVehicleService.execute(new CreateVehicleService.Command(
                orgId, req.licensePlate(), req.model(),
                VehicleType.valueOf(req.type().toUpperCase()),
                req.capacityKg(), req.costPerKmBrl()
        ));
        return Map.of("vehicleId", id);
    }

    @GetMapping
    public List<Vehicle> list() {
        return vehicleRepository.findAll(TenantContext.get());
    }
}
