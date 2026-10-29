package se.lexicon.parking.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Reservation {
    private final UUID reservationId;
    private final Person person;
    private final Vehicle vehicle;
    private final ParkingSpace parkingSpace;
    private final LocalDateTime startTime;
    private final Duration plannedDuration;
    private final LocalDateTime expectedEndTime;
    private LocalDateTime actualEndTime;
    private ReservationStatus status;

    public Reservation(Person person, Vehicle vehicle, ParkingSpace parkingSpace, Duration plannedDuration) {
        this(person, vehicle, parkingSpace, plannedDuration, LocalDateTime.now());
    }

    public Reservation(Person person, Vehicle vehicle, ParkingSpace parkingSpace,
                       Duration plannedDuration, LocalDateTime startTime) {
        this.person = Objects.requireNonNull(person, "person must not be null");
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle must not be null");
        if (!vehicle.getOwner().equals(person)) {
            throw new IllegalArgumentException("Vehicle must belong to the reserving person");
        }
        this.parkingSpace = Objects.requireNonNull(parkingSpace, "parkingSpace must not be null");
        this.plannedDuration = Objects.requireNonNull(plannedDuration, "plannedDuration must not be null");
        if (plannedDuration.isZero() || plannedDuration.isNegative()) {
            throw new IllegalArgumentException("plannedDuration must be positive");
        }
        this.startTime = Objects.requireNonNull(startTime, "startTime must not be null");
        this.expectedEndTime = startTime.plus(plannedDuration);
        this.reservationId = UUID.randomUUID();
        this.status = ReservationStatus.ACTIVE;
        parkingSpace.occupy();
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public Person getPerson() {
        return person;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSpace getParkingSpace() {
        return parkingSpace;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Duration getPlannedDuration() {
        return plannedDuration;
    }

    public LocalDateTime getExpectedEndTime() {
        return expectedEndTime;
    }

    public LocalDateTime getActualEndTime() {
        return actualEndTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void complete() {
        complete(LocalDateTime.now());
    }

    public void complete(LocalDateTime actualEndTime) {
        Objects.requireNonNull(actualEndTime, "actualEndTime must not be null");
        if (status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Reservation is already completed");
        }
        if (actualEndTime.isBefore(startTime)) {
            throw new IllegalArgumentException("actualEndTime cannot be before startTime");
        }
        this.actualEndTime = actualEndTime;
        this.status = ReservationStatus.COMPLETED;
        parkingSpace.release();
    }
}
