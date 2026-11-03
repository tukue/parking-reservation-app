package se.lexicon.parking.model;

/** @deprecated Use {@link ParkingPlaceStatus}. */
@Deprecated(forRemoval = true)
public final class SpaceStatus {
    public static final ParkingPlaceStatus AVAILABLE = ParkingPlaceStatus.AVAILABLE;
    public static final ParkingPlaceStatus OCCUPIED = ParkingPlaceStatus.OCCUPIED;

    private SpaceStatus() {
    }
}
