package se.lexicon.parking.model;

import java.util.Objects;

public class ParkingPlace {
    private final String spotId;
    private ParkingPlaceStatus status = ParkingPlaceStatus.AVAILABLE;

    public ParkingPlace(String spotId) {
        this.spotId = Person.requireText(spotId, "spotId");
    }

    public String getSpotId() {
        return spotId;
    }

    public ParkingPlaceStatus getStatus() {
        return status;
    }

    void occupy() {
        if (status == ParkingPlaceStatus.OCCUPIED) {
            throw new IllegalStateException("Parking spot " + spotId + " is already occupied");
        }
        status = ParkingPlaceStatus.OCCUPIED;
    }

    void release() {
        status = ParkingPlaceStatus.AVAILABLE;
    }
}
