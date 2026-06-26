package com.logicore.core.domain.model;

import com.logicore.shared.domain.exception.BusinessRuleException;
import com.logicore.shared.domain.valueobject.Money;
import com.logicore.shared.domain.valueobject.Weight;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class VehicleTest {

    private Vehicle createVehicle() {
        return Vehicle.create(
                UUID.randomUUID(),
                "ABC-1234",
                "Fiat Ducato",
                VehicleType.VAN,
                new Weight(1000.0),
                Money.brl(2.50)
        );
    }

    @Test
    void createStartsAsAvailable() {
        Vehicle vehicle = createVehicle();
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        assertThat(vehicle.isAvailable()).isTrue();
    }

    @Test
    void markInUseWorksWhenAvailable() {
        Vehicle vehicle = createVehicle();
        vehicle.markInUse();
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.IN_USE);
        assertThat(vehicle.isAvailable()).isFalse();
    }

    @Test
    void markInUseThrowsWhenAlreadyInUse() {
        Vehicle vehicle = createVehicle();
        vehicle.markInUse();
        assertThatThrownBy(vehicle::markInUse)
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void markAvailableSetsStatusBack() {
        Vehicle vehicle = createVehicle();
        vehicle.markInUse();
        vehicle.markAvailable();
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        assertThat(vehicle.isAvailable()).isTrue();
    }

    @Test
    void sendToMaintenanceSetsStatusAndNotes() {
        Vehicle vehicle = createVehicle();
        vehicle.sendToMaintenance("Motor com problema");
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.MAINTENANCE);
        assertThat(vehicle.getNotes()).isEqualTo("Motor com problema");
    }

    @Test
    void inactivateSetsStatusInactive() {
        Vehicle vehicle = createVehicle();
        vehicle.inactivate();
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.INACTIVE);
    }

    @Test
    void createWithNullOrgIdThrows() {
        assertThatThrownBy(() -> Vehicle.create(null, "ABC-1234", "Model", VehicleType.VAN, new Weight(1000), Money.brl(2.0)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
