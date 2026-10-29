package se.lexicon.parking.model;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParkingAreaTest {
    @Test
    void hasTwentyAvailableSpacesInitiallyAndRestoresAvailabilityAfterReservation() {
        ParkingArea parkingArea = new ParkingArea("Lexicon Växjö");
        Person person = new Person("Alex Example", "+46123456789");
        Vehicle vehicle = new Vehicle("ABC123", person);

        assertEquals(ParkingArea.SPACE_COUNT, parkingArea.getSpaces().size());
        assertEquals(ParkingArea.SPACE_COUNT, parkingArea.getAvailableSpaces().size());

        ParkingSpace selectedSpace = parkingArea.getSpaces().getFirst();
        LocalDateTime startTime = LocalDateTime.of(2026, 10, 29, 10, 0);
        Reservation reservation = new Reservation(
                person, vehicle, selectedSpace, Duration.ofHours(1), startTime);

        assertEquals(ParkingArea.SPACE_COUNT - 1, parkingArea.getAvailableSpaces().size());
        reservation.complete(startTime.plusMinutes(30));
        assertEquals(ParkingArea.SPACE_COUNT, parkingArea.getAvailableSpaces().size());
    }

    @Test
    void rejectsBlankParkingAreaName() {
        assertThrows(IllegalArgumentException.class, () -> new ParkingArea(" "));
    }
}
