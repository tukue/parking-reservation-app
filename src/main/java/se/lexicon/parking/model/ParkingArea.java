package se.lexicon.parking.model;

import java.util.List;

public class ParkingArea {
    public static final int SPACE_COUNT = 20;

    private final String name;
    private final List<ParkingSpace> spaces;

    public ParkingArea(String name) {
        this.name = Person.requireText(name, "name");
        this.spaces = java.util.stream.IntStream.rangeClosed(1, SPACE_COUNT)
                .mapToObj(number -> new ParkingSpace(Integer.toString(number)))
                .toList();
    }

    public String getName() {
        return name;
    }

    public List<ParkingSpace> getSpaces() {
        return spaces;
    }

    public List<ParkingSpace> getAvailableSpaces() {
        return spaces.stream()
                .filter(space -> space.getStatus() == SpaceStatus.AVAILABLE)
                .toList();
    }
}
