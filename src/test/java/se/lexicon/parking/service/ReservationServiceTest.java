
package se.lexicon.parking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import se.lexicon.parking.model.ParkingSpace;
import se.lexicon.parking.model.Person;
import se.lexicon.parking.model.Reservation;
import se.lexicon.parking.model.ReservationStatus;
import se.lexicon.parking.model.SpaceStatus;
import se.lexicon.parking.model.Vehicle;
import se.lexicon.parking.storage.memory.InMemoryParkingStore;

import static org.junit.jupiter.api.Assertions.*;

class ReservationServiceTest {

    private InMemoryParkingStore store;
    private ReservationService reservationService;

    private Person person;
    private Vehicle vehicle;
    private ParkingSpace parkingSpace;

    @BeforeEach
    void setUp() {
        store = new InMemoryParkingStore("Test Parking");

        reservationService = new ReservationService(
                store.parkingArea(),
                store.people(),
                store.reservations());

        person = new Person("Padma", "0701234567");
        vehicle = new Vehicle("ABC123", person);
        parkingSpace = store.parkingArea().getSpaces().getFirst();

        // Register the person before creating reservations.
        store.people().save(person);
    }

    @Test
    void createReservation_shouldReserveAvailableSpace() {
        Reservation reservation = reservationService.createReservation(
                person, vehicle, parkingSpace, Duration.ofHours(2));

        assertNotNull(reservation.getReservationId());
        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());
        assertEquals(SpaceStatus.OCCUPIED, parkingSpace.getStatus());
        assertEquals(1, reservationService.getActiveReservations().size());
        assertEquals(1, reservationService.getAllReservations().size());
    }

    @Test
    void createReservation_shouldRejectOccupiedSpace() {
        reservationService.createReservation(
                person, vehicle, parkingSpace, Duration.ofHours(2));

        assertThrows(IllegalStateException.class,
                () -> reservationService.createReservation(
                        person, vehicle, parkingSpace,
                        Duration.ofHours(1)));

        assertEquals(1, reservationService.getAllReservations().size());
    }

    @Test
    void createReservation_shouldRejectUnregisteredPerson() {
        Person unregisteredPerson =
                new Person("Unknown", "0709999999");

        Vehicle unregisteredVehicle =
                new Vehicle("XYZ789", unregisteredPerson);

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(
                        unregisteredPerson,
                        unregisteredVehicle,
                        parkingSpace,
                        Duration.ofHours(1)));

        assertEquals(SpaceStatus.AVAILABLE, parkingSpace.getStatus());
        assertTrue(reservationService.getAllReservations().isEmpty());
    }

    @Test
    void createReservation_shouldRejectVehicleOwnedByAnotherPerson() {
        Person anotherPerson =
                new Person("Another Person", "0705555555");

        Vehicle anotherPersonsVehicle =
                new Vehicle("XYZ789", anotherPerson);

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(
                        person,
                        anotherPersonsVehicle,
                        parkingSpace,
                        Duration.ofHours(1)));

        assertEquals(SpaceStatus.AVAILABLE, parkingSpace.getStatus());
        assertTrue(reservationService.getAllReservations().isEmpty());
    }

    @Test
    void completeReservation_shouldReleaseSpaceAndUpdateStatus() {
        Reservation reservation = reservationService.createReservation(
                person, vehicle, parkingSpace, Duration.ofHours(2));

        LocalDateTime endTime =
                reservation.getStartTime().plusHours(1);

        Reservation completed = reservationService.completeReservation(
                reservation.getReservationId(), endTime);

        assertEquals(ReservationStatus.COMPLETED, completed.getStatus());
        assertEquals(endTime, completed.getActualEndTime());
        assertEquals(SpaceStatus.AVAILABLE, parkingSpace.getStatus());
        assertTrue(reservationService.getActiveReservations().isEmpty());
        assertEquals(1, reservationService.getAllReservations().size());
    }

    @Test
    void completeReservation_shouldRejectAlreadyCompletedReservation() {
        Reservation reservation = reservationService.createReservation(
                person, vehicle, parkingSpace, Duration.ofHours(2));

        UUID reservationId = reservation.getReservationId();

        reservationService.completeReservation(
                reservationId,
                reservation.getStartTime().plusHours(1));

        assertThrows(IllegalStateException.class,
                () -> reservationService.completeReservation(
                        reservationId,
                        reservation.getStartTime().plusHours(2)));

        assertEquals(SpaceStatus.AVAILABLE, parkingSpace.getStatus());
        assertEquals(1, reservationService.getAllReservations().size());
    }

    @Test
    void completeReservation_shouldRejectUnknownReservation() {
        assertThrows(IllegalArgumentException.class,
                () -> reservationService.completeReservation(
                        UUID.randomUUID()));
    }

    @Test
    void getAvailableSpaces_shouldUpdateAfterReservationAndCompletion() {
        assertEquals(20, reservationService.getAllSpaces().size());
        assertEquals(20, reservationService.getAvailableSpaces().size());

        Reservation reservation = reservationService.createReservation(
                person, vehicle, parkingSpace, Duration.ofHours(2));

        assertEquals(19, reservationService.getAvailableSpaces().size());
        assertFalse(
                reservationService.getAvailableSpaces().contains(parkingSpace));

        reservationService.completeReservation(
                reservation.getReservationId(),
                reservation.getStartTime().plusHours(1));

        assertEquals(20, reservationService.getAvailableSpaces().size());
        assertTrue(
                reservationService.getAvailableSpaces().contains(parkingSpace));
    }

    @Test
    void createReservation_shouldRejectSpaceFromAnotherParkingArea() {
        InMemoryParkingStore anotherStore =
                new InMemoryParkingStore("Another Parking");

        ParkingSpace foreignSpace =
                anotherStore.parkingArea().getSpaces().getFirst();

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(
                        person, vehicle, foreignSpace,
                        Duration.ofHours(1)));

        assertEquals(SpaceStatus.AVAILABLE, parkingSpace.getStatus());
        assertTrue(reservationService.getAllReservations().isEmpty());
    }
}
