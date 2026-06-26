package com.logicore.core.domain.model;

import com.logicore.shared.domain.valueobject.Address;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import com.logicore.shared.domain.valueobject.Priority;
import com.logicore.shared.domain.valueobject.TimeWindow;

import java.time.Instant;
import java.util.UUID;

public class Customer {

    private final UUID id;
    private final UUID organizationId;
    private final String name;
    private final String email;
    private final String phone;
    private final Address address;
    private GeoCoordinate location;
    private final TimeWindow deliveryWindow;
    private final Priority priority;
    private boolean active;
    private final Instant createdAt;

    private Customer(UUID id, UUID organizationId, String name, String email, String phone,
                     Address address, GeoCoordinate location, TimeWindow deliveryWindow,
                     Priority priority, boolean active, Instant createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.location = location;
        this.deliveryWindow = deliveryWindow;
        this.priority = priority;
        this.active = active;
        this.createdAt = createdAt;
    }

    public static Customer create(UUID orgId, String name, String email, String phone,
                                  Address address, GeoCoordinate location,
                                  TimeWindow deliveryWindow, Priority priority) {
        if (orgId == null) throw new IllegalArgumentException("Organization ID é obrigatório");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Nome do cliente é obrigatório");
        if (address == null) throw new IllegalArgumentException("Endereço é obrigatório");
        if (location == null) throw new IllegalArgumentException("Localização geográfica é obrigatória");

        return new Customer(
                UUID.randomUUID(), orgId, name, email, phone,
                address, location, deliveryWindow, priority, true, Instant.now()
        );
    }

    public static Customer reconstitute(UUID id, UUID organizationId, String name, String email, String phone,
                                        Address address, GeoCoordinate location, TimeWindow deliveryWindow,
                                        Priority priority, boolean active, Instant createdAt) {
        return new Customer(id, organizationId, name, email, phone,
                address, location, deliveryWindow, priority, active, createdAt);
    }

    public void updateLocation(GeoCoordinate newLocation) {
        if (newLocation == null) throw new IllegalArgumentException("Nova localização é obrigatória");
        this.location = newLocation;
    }

    public void deactivate() {
        this.active = false;
    }

    public UUID getId() { return id; }
    public UUID getOrganizationId() { return organizationId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Address getAddress() { return address; }
    public GeoCoordinate getLocation() { return location; }
    public TimeWindow getDeliveryWindow() { return deliveryWindow; }
    public Priority getPriority() { return priority; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}
