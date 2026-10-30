package se.lexicon.parking.model;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void rejectsInvalidDurationWithoutOccupyingSpace() {
        Person person = new Person("Alex Example", "+46123456789");
        Vehicle vehicle = new Vehicle("ABC123", person);
        ParkingSpace parkingSpace = new ParkingSpace("1");

        assertThrows(IllegalArgumentException.class,
                () -> new Reservation(person, vehicle, parkingSpace, Duration.ZERO));
        assertEquals(SpaceStatus.AVAILABLE, parkingSpace.getStatus());
    }

    @Test
    void completingReservationRecordsEndTimeAndReleasesSpace() {
        Person person = new Person("Alex Example", "+46123456789");
        Vehicle vehicle = new Vehicle("ABC123", person);
        ParkingSpace parkingSpace = new ParkingSpace("1");
        LocalDateTime startTime = LocalDateTime.of(2026, 10, 29, 10, 0);
        LocalDateTime actualEndTime = startTime.plusMinutes(45);
        Reservation reservation = new Reservation(
                person, vehicle, parkingSpace, Duration.ofHours(2), startTime);

        reservation.complete(actualEndTime);

        assertEquals(ReservationStatus.COMPLETED, reservation.getStatus());
        assertEquals(actualEndTime, reservation.getActualEndTime());
        assertEquals(SpaceStatus.AVAILABLE, parkingSpace.getStatus());
    }

    @Test
    void cannotCompleteReservationMoreThanOnce() {
        Person person = new Person("Alex Example", "+46123456789");
        Vehicle vehicle = new Vehicle("ABC123", person);
        ParkingSpace parkingSpace = new ParkingSpace("1");
        LocalDateTime startTime = LocalDateTime.of(2026, 10, 29, 10, 0);
        Reservation reservation = new Reservation(
                person, vehicle, parkingSpace, Duration.ofHours(2), startTime);
        LocalDateTime firstEndTime = startTime.plusMinutes(45);

        reservation.complete(firstEndTime);

        assertThrows(IllegalStateException.class,
                () -> reservation.complete(firstEndTime.plusMinutes(15)));
        assertEquals(firstEndTime, reservation.getActualEndTime());
        assertEquals(SpaceStatus.AVAILABLE, parkingSpace.getStatus());
    }

    @Test
    void cancellingReservationRecordsTimeAndReleasesSpace() {
        Person person = new Person("Alex Example", "+46123456789");
        ParkingSpace parkingSpace = new ParkingSpace("1");
        LocalDateTime startTime = LocalDateTime.of(2026, 10, 29, 10, 0);
        LocalDateTime cancelledAt = startTime.plusMinutes(15);
        Reservation reservation = new Reservation(person, new Vehicle("ABC123", person), parkingSpace,
                Duration.ofHours(2), startTime);

        reservation.cancel(cancelledAt);

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        assertEquals(cancelledAt, reservation.getCancelledAt());
        assertEquals(SpaceStatus.AVAILABLE, parkingSpace.getStatus());
    }

    @Test
    void cannotCancelReservationMoreThanOnceOrCompleteItAfterCancellation() {
        Person person = new Person("Alex Example", "+46123456789");
        ParkingSpace parkingSpace = new ParkingSpace("1");
        LocalDateTime startTime = LocalDateTime.of(2026, 10, 29, 10, 0);
        Reservation reservation = new Reservation(person, new Vehicle("ABC123", person), parkingSpace,
                Duration.ofHours(2), startTime);
        LocalDateTime cancelledAt = startTime.plusMinutes(15);
        reservation.cancel(cancelledAt);

        assertThrows(IllegalStateException.class, () -> reservation.cancel(cancelledAt.plusMinutes(1)));
        assertThrows(IllegalStateException.class, () -> reservation.complete(cancelledAt.plusMinutes(1)));
        assertEquals(cancelledAt, reservation.getCancelledAt());
        assertEquals(SpaceStatus.AVAILABLE, parkingSpace.getStatus());
    }
}
