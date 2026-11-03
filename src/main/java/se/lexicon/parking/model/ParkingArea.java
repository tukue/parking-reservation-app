package se.lexicon.parking.model;

/** @deprecated Use {@link ParkingLot}. */
@Deprecated(forRemoval = true)
public class ParkingArea extends ParkingLot {
    public static final int SPACE_COUNT = PLACE_COUNT;

    public ParkingArea(String name) {
        super(name);
    }
}
