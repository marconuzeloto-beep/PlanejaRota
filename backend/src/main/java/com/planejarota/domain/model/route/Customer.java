package com.planejarota.domain.model.route;

import com.planejarota.domain.valueobject.Address;
import com.planejarota.domain.valueobject.GeoCoordinate;
import com.planejarota.domain.valueobject.TimeWindow;

import java.util.UUID;

/**
 * Entidade de domínio representando um cliente de entrega.
 *
 * O cliente é dono do seu endereço e janela de tempo preferencial.
 * A coordenada geográfica é obtida via geocodificação do endereço.
 */
public class Customer {

    private final UUID id;
    private String name;
    private String email;
    private String phone;
    private Address address;
    private GeoCoordinate location;
    private TimeWindow preferredDeliveryWindow;
    private int priority;

    private Customer() {}

    public static Customer create(
            String name,
            String email,
            String phone,
            Address address,
            GeoCoordinate location,
            TimeWindow preferredDeliveryWindow,
            int priority
    ) {
        validate(name, address, location);
        Customer customer = new Customer();
        customer.id = UUID.randomUUID();
        customer.name = name;
        customer.email = email;
        customer.phone = phone;
        customer.address = address;
        customer.location = location;
        customer.preferredDeliveryWindow = preferredDeliveryWindow;
        customer.priority = Math.max(1, Math.min(5, priority));
        return customer;
    }

    public static Customer reconstitute(
            UUID id, String name, String email, String phone,
            Address address, GeoCoordinate location,
            TimeWindow preferredDeliveryWindow, int priority
    ) {
        Customer customer = new Customer();
        customer.id = id;
        customer.name = name;
        customer.email = email;
        customer.phone = phone;
        customer.address = address;
        customer.location = location;
        customer.preferredDeliveryWindow = preferredDeliveryWindow;
        customer.priority = priority;
        return customer;
    }

    private static void validate(String name, Address address, GeoCoordinate location) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Nome do cliente é obrigatório");
        if (address == null) throw new IllegalArgumentException("Endereço é obrigatório");
        if (location == null) throw new IllegalArgumentException("Localização geográfica é obrigatória");
    }

    public void updateLocation(GeoCoordinate newLocation) {
        if (newLocation == null) throw new IllegalArgumentException("Localização não pode ser nula");
        this.location = newLocation;
    }

    public void updatePreferredDeliveryWindow(TimeWindow window) {
        this.preferredDeliveryWindow = window;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Address getAddress() { return address; }
    public GeoCoordinate getLocation() { return location; }
    public TimeWindow getPreferredDeliveryWindow() { return preferredDeliveryWindow; }
    public int getPriority() { return priority; }
}
