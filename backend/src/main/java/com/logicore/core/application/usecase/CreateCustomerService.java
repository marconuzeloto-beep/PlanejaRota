package com.logicore.core.application.usecase;

import com.logicore.core.domain.model.Customer;
import com.logicore.core.domain.repository.CustomerRepository;
import com.logicore.shared.domain.valueobject.Address;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import com.logicore.shared.domain.valueobject.Priority;
import com.logicore.shared.domain.valueobject.TimeWindow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateCustomerService {

    private final CustomerRepository customerRepository;

    public record Command(
            UUID orgId,
            String name,
            String email,
            String phone,
            Address address,
            GeoCoordinate location,
            TimeWindow deliveryWindow,
            int priority
    ) {}

    public UUID execute(Command command) {
        if (command.orgId() == null) {
            throw new IllegalArgumentException("Organization ID é obrigatório");
        }

        Customer customer = Customer.create(
                command.orgId(),
                command.name(),
                command.email(),
                command.phone(),
                command.address(),
                command.location(),
                command.deliveryWindow(),
                new Priority(command.priority())
        );

        Customer saved = customerRepository.save(customer);
        return saved.getId();
    }
}
