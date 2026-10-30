package se.lexicon.parking;

public class ParkingArea {
    private String name;
    private int capacity;

    // Constructor to initialize the parking area name and maximum capacity
    public ParkingArea(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}