package com.logicore.core.infrastructure.persistence.mapper;

import com.logicore.core.domain.model.Customer;
import com.logicore.core.infrastructure.persistence.entity.CustomerJpaEntity;
import com.logicore.shared.domain.valueobject.Address;
import com.logicore.shared.domain.valueobject.GeoCoordinate;
import com.logicore.shared.domain.valueobject.Priority;
import com.logicore.shared.domain.valueobject.TimeWindow;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    public Customer toDomain(CustomerJpaEntity e) {
        GeoCoordinate location = new GeoCoordinate(e.getLocation().getY(), e.getLocation().getX());

        TimeWindow timeWindow = (e.getTwOpen() != null && e.getTwClose() != null)
                ? new TimeWindow(e.getTwOpen(), e.getTwClose())
                : null;

        Address address = new Address(
                e.getStreet(),
                e.getAddressNumber(),
                e.getComplement(),
                e.getNeighborhood(),
                e.getCity(),
                e.getState(),
                e.getZipCode(),
                e.getCountry()
        );

        Priority priority = new Priority(e.getPriority());

        return Customer.reconstitute(
                e.getId(),
                e.getOrganizationId(),
                e.getName(),
                e.getEmail(),
                e.getPhone(),
                address,
                location,
                timeWindow,
                priority,
                e.isActive(),
                e.getCreatedAt()
        );
    }

    public CustomerJpaEntity toEntity(Customer customer) {
        CustomerJpaEntity e = new CustomerJpaEntity();
        e.setId(customer.getId());
        e.setOrganizationId(customer.getOrganizationId());
        e.setName(customer.getName());
        e.setEmail(customer.getEmail());
        e.setPhone(customer.getPhone());

        Address addr = customer.getAddress();
        e.setStreet(addr.street());
        e.setAddressNumber(addr.number());
        e.setComplement(addr.complement());
        e.setNeighborhood(addr.neighborhood());
        e.setCity(addr.city());
        e.setState(addr.state());
        e.setZipCode(addr.zipCode());
        e.setCountry(addr.country());

        GeoCoordinate coord = customer.getLocation();
        Point point = GF.createPoint(new Coordinate(coord.longitude(), coord.latitude()));
        e.setLocation(point);

        if (customer.getDeliveryWindow() != null) {
            e.setTwOpen(customer.getDeliveryWindow().openTime());
            e.setTwClose(customer.getDeliveryWindow().closeTime());
        }

        e.setPriority(customer.getPriority().value());
        e.setActive(customer.isActive());
        e.setCreatedAt(customer.getCreatedAt());

        return e;
    }
}
