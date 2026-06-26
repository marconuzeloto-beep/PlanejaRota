package com.logicore.core.application.usecase;

import com.logicore.core.domain.model.DeliveryOrder;
import com.logicore.core.domain.repository.CustomerRepository;
import com.logicore.core.domain.repository.OrderRepository;
import com.logicore.shared.domain.exception.BusinessRuleException;
import com.logicore.shared.domain.exception.ResourceNotFoundException;
import com.logicore.shared.domain.valueobject.Money;
import com.logicore.shared.domain.valueobject.TimeWindow;
import com.logicore.shared.domain.valueobject.Weight;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateOrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public record Command(
            UUID orgId,
            UUID customerId,
            String orderCode,
            String description,
            double weightKg,
            BigDecimal declaredValue,
            LocalDate deliveryDate,
            LocalTime twOpen,
            LocalTime twClose
    ) {}

    public UUID execute(Command command) {
        if (orderRepository.existsByOrderCode(command.orgId(), command.orderCode())) {
            throw new BusinessRuleException(
                    "Já existe um pedido com o código: " + command.orderCode());
        }

        if (!customerRepository.existsById(command.orgId(), command.customerId())) {
            throw new ResourceNotFoundException(
                    "Cliente não encontrado: " + command.customerId());
        }

        TimeWindow timeWindow = (command.twOpen() != null && command.twClose() != null)
                ? new TimeWindow(command.twOpen(), command.twClose())
                : null;

        Money declaredValue = (command.declaredValue() != null)
                ? Money.brl(command.declaredValue())
                : null;

        DeliveryOrder order = DeliveryOrder.create(
                command.orgId(),
                command.customerId(),
                command.orderCode(),
                command.description(),
                new Weight(command.weightKg()),
                declaredValue,
                command.deliveryDate(),
                timeWindow
        );

        DeliveryOrder saved = orderRepository.save(order);
        return saved.getId();
    }
}
