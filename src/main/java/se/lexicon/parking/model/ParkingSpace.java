package se.lexicon.parking.model;

import java.util.Objects;

public class ParkingSpace {
    private final String spaceId;
    private SpaceStatus status = SpaceStatus.AVAILABLE;

    public ParkingSpace(String spaceId) {
        this.spaceId = Person.requireText(spaceId, "spaceId");
    }

    public String getSpaceId() {
        return spaceId;
    }

    public SpaceStatus getStatus() {
        return status;
    }

    void occupy() {
        if (status == SpaceStatus.OCCUPIED) {
            throw new IllegalStateException("Parking space " + spaceId + " is already occupied");
        }
        status = SpaceStatus.OCCUPIED;
    }

    void release() {
        status = SpaceStatus.AVAILABLE;
    }
}
