package com.logicore.api.controller;

import com.logicore.api.dto.request.CreateCustomerRequest;
import com.logicore.core.application.usecase.CreateCustomerService;
import com.logicore.core.domain.model.Customer;
import com.logicore.core.domain.repository.CustomerRepository;
import com.logicore.shared.domain.valueobject.Address;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import com.logicore.shared.infrastructure.tenant.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Customers", description = "Gestão de clientes")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CreateCustomerService createCustomerService;
    private final CustomerRepository customerRepository;

    public CustomerController(CreateCustomerService createCustomerService,
                               CustomerRepository customerRepository) {
        this.createCustomerService = createCustomerService;
        this.customerRepository = customerRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> create(@Valid @RequestBody CreateCustomerRequest req) {
        UUID orgId = TenantContext.get();

        Address address = new Address(
                req.street(), req.number(), req.complement(),
                req.neighborhood(), req.city(), req.state(), req.zipCode(), "Brasil");

        UUID id = createCustomerService.execute(new CreateCustomerService.Command(
                orgId, req.name(), req.email(), req.phone(),
                address,
                new GeoCoordinate(req.latitude(), req.longitude()),
                null,
                req.priority()
        ));

        return Map.of("customerId", id);
    }

    @GetMapping
    public List<Customer> list() {
        return customerRepository.findAll(TenantContext.get());
    }
}
