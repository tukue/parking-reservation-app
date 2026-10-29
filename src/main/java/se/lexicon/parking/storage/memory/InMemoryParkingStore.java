package se.lexicon.parking.storage.memory;

import java.util.Objects;

import se.lexicon.parking.model.ParkingArea;
import se.lexicon.parking.storage.PersonRepository;
import se.lexicon.parking.storage.ReservationRepository;

/** Holds one shared in-memory application state for the lifetime of the process. */
public class InMemoryParkingStore {
    private final PersonRepository people;
    private final ReservationRepository reservations;
    private final ParkingArea parkingArea;

    public InMemoryParkingStore(String parkingAreaName) {
        this.people = new InMemoryPersonRepository();
        this.reservations = new InMemoryReservationRepository();
        this.parkingArea = new ParkingArea(Objects.requireNonNull(parkingAreaName,
                "parkingAreaName must not be null"));
    }

    public PersonRepository people() {
        return people;
    }

    public ReservationRepository reservations() {
        return reservations;
    }

    public ParkingArea parkingArea() {
        return parkingArea;
    }
}
