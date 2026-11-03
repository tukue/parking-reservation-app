package se.lexicon.parking.storage.memory;

import java.util.Objects;

import se.lexicon.parking.model.ParkingLot;
import se.lexicon.parking.storage.PersonRepository;
import se.lexicon.parking.storage.ReservationRepository;

/** Holds one shared in-memory application state for the lifetime of the process. */
public class InMemoryParkingStore {
    private final PersonRepository people;
    private final ReservationRepository reservations;
    private final ParkingLot parkingLot;

    public InMemoryParkingStore(String parkingLotName) {
        this.people = new InMemoryPersonRepository();
        this.reservations = new InMemoryReservationRepository();
        this.parkingLot = new ParkingLot(Objects.requireNonNull(parkingLotName,
                "parkingLotName must not be null"));
    }

    public PersonRepository people() {
        return people;
    }

    public ReservationRepository reservations() {
        return reservations;
    }

    public ParkingLot parkingLot() {
        return parkingLot;
    }

}
