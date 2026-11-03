package se.lexicon.parking.model;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParkingLotTest {
    @Test
    void hasTwentyAvailablePlacesInitiallyAndRestoresAvailabilityAfterReservation() {
        ParkingLot parkingLot = new ParkingLot("Lexicon Växjö");
        Person person = new Person("Alex Example", "+46123456789");
        Vehicle vehicle = new Vehicle("ABC123", person);

        assertEquals(ParkingLot.PLACE_COUNT, parkingLot.getParkingPlaces().size());
        assertEquals(ParkingLot.PLACE_COUNT, parkingLot.getAvailableParkingPlaces().size());

        ParkingPlace selectedPlace = parkingLot.getParkingPlaces().getFirst();
        LocalDateTime startTime = LocalDateTime.of(2026, 10, 29, 10, 0);
        Reservation reservation = new Reservation(
                person, vehicle, selectedPlace, Duration.ofHours(1), startTime);

        assertEquals(ParkingLot.PLACE_COUNT - 1, parkingLot.getAvailableParkingPlaces().size());
        reservation.complete(startTime.plusMinutes(30));
        assertEquals(ParkingLot.PLACE_COUNT, parkingLot.getAvailableParkingPlaces().size());
    }

    @Test
    void rejectsBlankParkingLotName() {
        assertThrows(IllegalArgumentException.class, () -> new ParkingLot(" "));
    }
}
