package se.lexicon.parking;

public class ParkingSpace {
    private String spaceId;
    private SpaceStatus status; // Uses the SpaceStatus enum you created!

    // Constructor to initialize the space ID and its status
    public ParkingSpace(String spaceId, SpaceStatus status) {
        this.spaceId = spaceId;
        this.status = status;
    }

    // Getters and Setters
    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public SpaceStatus getStatus() {
        return status;
    }

    public void setStatus(SpaceStatus status) {
        this.status = status;
    }
}