package com.logicore.core.domain.model;

import com.logicore.shared.domain.valueobject.*;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class CustomerTest {

    private Address defaultAddress() {
        return new Address("Rua das Flores", "100", null, "Centro", "São Paulo", "SP", "01001-000", "Brasil");
    }

    private GeoCoordinate defaultLocation() {
        return new GeoCoordinate(-23.5505, -46.6333);
    }

    @Test
    void createWithValidDataSucceeds() {
        Customer customer = Customer.create(
                UUID.randomUUID(),
                "João Silva",
                "joao@email.com",
                "(11) 99999-9999",
                defaultAddress(),
                defaultLocation(),
                new TimeWindow(LocalTime.of(8, 0), LocalTime.of(18, 0)),
                new Priority(3)
        );
        assertThat(customer.getName()).isEqualTo("João Silva");
        assertThat(customer.isActive()).isTrue();
        assertThat(customer.getLocation()).isEqualTo(defaultLocation());
    }

    @Test
    void createWithoutLocationThrows() {
        assertThatThrownBy(() -> Customer.create(
                UUID.randomUUID(),
                "João Silva",
                "joao@email.com",
                "(11) 99999-9999",
                defaultAddress(),
                null,
                null,
                new Priority(3)
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createWithoutAddressThrows() {
        assertThatThrownBy(() -> Customer.create(
                UUID.randomUUID(),
                "João Silva",
                "joao@email.com",
                "(11) 99999-9999",
                null,
                defaultLocation(),
                null,
                new Priority(3)
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deactivateSetsActiveToFalse() {
        Customer customer = Customer.create(
                UUID.randomUUID(),
                "Maria Souza",
                null,
                null,
                defaultAddress(),
                defaultLocation(),
                null,
                new Priority(1)
        );
        assertThat(customer.isActive()).isTrue();
        customer.deactivate();
        assertThat(customer.isActive()).isFalse();
    }
}
