package com.logicore.api.controller;

import com.logicore.api.dto.request.CreateOrderRequest;
import com.logicore.core.application.usecase.CreateOrderService;
import com.logicore.core.domain.model.DeliveryOrder;
import com.logicore.core.domain.repository.OrderRepository;
import com.logicore.shared.infrastructure.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CreateOrderService createOrderService;
    private final OrderRepository orderRepository;

    public OrderController(CreateOrderService createOrderService, OrderRepository orderRepository) {
        this.createOrderService = createOrderService;
        this.orderRepository = orderRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> create(@Valid @RequestBody CreateOrderRequest req) {
        UUID orgId = TenantContext.get();
        UUID id = createOrderService.execute(new CreateOrderService.Command(
                orgId, req.customerId(), req.orderCode(), req.description(),
                req.weightKg(), req.declaredValueBrl(), req.deliveryDate(),
                null, null
        ));
        return Map.of("orderId", id);
    }

    @GetMapping
    public List<DeliveryOrder> list() {
        return orderRepository.findAll(TenantContext.get());
    }
}
