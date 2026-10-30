package se.lexicon.parking;

import java.util.UUID;
import java.time.LocalDateTime;
import java.time.Duration;

public class Reservation {
    private UUID reservationId;
    private LocalDateTime startTime;
    private Duration plannedDuration;
    private LocalDateTime expectedEndTime;
    private LocalDateTime actualEndTime;
    private ReservationStatus status;
    private Vehicle vehicle;
    private ParkingSpace parkingSpace;

    // Constructor to initialize a new reservation
    public Reservation(LocalDateTime startTime, Duration plannedDuration, Vehicle vehicle, ParkingSpace parkingSpace) {
        this.reservationId = UUID.randomUUID(); // Automatically generates a unique ID
        this.startTime = startTime;
        this.plannedDuration = plannedDuration;
        this.expectedEndTime = startTime.plus(plannedDuration); // Automatically calculates expected end time
        this.status = ReservationStatus.ACTIVE;
        this.vehicle = vehicle;
        this.parkingSpace = parkingSpace;
    }

    // Getters and Setters
    public UUID getReservationId() {
        return reservationId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getPlannedDuration() {
        return plannedDuration;
    }

    public void setPlannedDuration(Duration plannedDuration) {
        this.plannedDuration = plannedDuration;
        this.expectedEndTime = this.startTime.plus(plannedDuration);
    }

    public LocalDateTime getExpectedEndTime() {
        return expectedEndTime;
    }

    public LocalDateTime getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(LocalDateTime actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public ParkingSpace getParkingSpace() {
        return parkingSpace;
    }

    public void setParkingSpace(ParkingSpace parkingSpace) {
        this.parkingSpace = parkingSpace;
    }
}