package se.lexicon.parking.model;

import java.util.List;

public class ParkingLot {
    public static final int PLACE_COUNT = 20;

    private final String name;
    private final List<ParkingPlace> parkingPlaces;

    public ParkingLot(String name) {
        this.name = Person.requireText(name, "name");
        this.parkingPlaces = java.util.stream.IntStream.rangeClosed(1, PLACE_COUNT)
                .mapToObj(number -> new ParkingPlace(Integer.toString(number)))
                .toList();
    }

    public String getName() {
        return name;
    }

    public List<ParkingPlace> getParkingPlaces() {
        return parkingPlaces;
    }

    /** @deprecated Use {@link #getParkingPlaces()}. */
    @Deprecated(forRemoval = true)
    public List<ParkingPlace> getSpaces() {
        return getParkingPlaces();
    }

    public List<ParkingPlace> getAvailableParkingPlaces() {
        return parkingPlaces.stream()
                .filter(place -> place.getStatus() == ParkingPlaceStatus.AVAILABLE)
                .toList();
    }

    /** @deprecated Use {@link #getAvailableParkingPlaces()}. */
    @Deprecated(forRemoval = true)
    public List<ParkingPlace> getAvailableSpaces() {
        return getAvailableParkingPlaces();
    }
}
