package se.lexicon.parking.storage.memory;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import se.lexicon.parking.model.ParkingSpace;
import se.lexicon.parking.model.Person;
import se.lexicon.parking.model.Reservation;
import se.lexicon.parking.model.Vehicle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryReservationRepositoryTest {
    @Test
    void savesAndFindsReservationById() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        Reservation reservation = createReservation("1");

        repository.save(reservation);

        assertEquals(reservation,
                repository.findById(reservation.getReservationId()).orElseThrow());
        assertEquals(1, repository.findAll().size());
        assertEquals(1, repository.findActive().size());
    }

    @Test
    void activeReservationsReflectCompletion() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        Reservation reservation = createReservation("1");
        repository.save(reservation);

        reservation.complete(LocalDateTime.of(2026, 10, 29, 10, 30));

        assertTrue(repository.findActive().isEmpty());
        assertEquals(1, repository.findAll().size());
    }

    private Reservation createReservation(String spaceId) {
        Person person = new Person("Alex Example", "+46123456789");
        Vehicle vehicle = new Vehicle("ABC123", person);
        return new Reservation(person, vehicle, new ParkingSpace(spaceId), Duration.ofHours(1),
                LocalDateTime.of(2026, 10, 29, 10, 0));
    }
}
