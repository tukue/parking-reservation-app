package se.lexicon.parking.model;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationTest {
    @Test
    void acceptsEquivalentPersonInstanceAsVehicleOwner() {
        Person reservingPerson = new Person("Alex Example", "+46123456789");
        Person vehicleOwner = new Person("Alex Example", "+46123456789");
        Vehicle vehicle = new Vehicle("ABC123", vehicleOwner);
        ParkingSpace parkingSpace = new ParkingSpace("1");
        LocalDateTime startTime = LocalDateTime.of(2026, 10, 29, 10, 0);

        Reservation reservation = new Reservation(
                reservingPerson, vehicle, parkingSpace, Duration.ofHours(2), startTime);

        assertEquals(reservingPerson, reservation.getPerson());
        assertEquals(SpaceStatus.OCCUPIED, parkingSpace.getStatus());
        assertEquals(startTime.plusHours(2), reservation.getExpectedEndTime());
    }
}
