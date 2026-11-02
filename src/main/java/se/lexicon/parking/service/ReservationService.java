
package se.lexicon.parking.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.DateTimeException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import se.lexicon.parking.model.ParkingArea;
import se.lexicon.parking.model.ParkingSpace;
import se.lexicon.parking.model.Person;
import se.lexicon.parking.model.Reservation;
import se.lexicon.parking.model.ReservationStatus;
import se.lexicon.parking.model.SpaceStatus;
import se.lexicon.parking.model.Vehicle;
import se.lexicon.parking.storage.PersonRepository;
import se.lexicon.parking.storage.ReservationRepository;

public class ReservationService {

    private final ParkingArea parkingArea;
    private final PersonRepository personRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(
            ParkingArea parkingArea,
            PersonRepository personRepository,
            ReservationRepository reservationRepository) {

        this.parkingArea = Objects.requireNonNull(
                parkingArea, "parkingArea must not be null");

        this.personRepository = Objects.requireNonNull(
                personRepository, "personRepository must not be null");

        this.reservationRepository = Objects.requireNonNull(
                reservationRepository, "reservationRepository must not be null");
    }

    public synchronized Person registerPerson(Person person) {
        Objects.requireNonNull(person, "person must not be null");
        Optional<Person> existingPerson = personRepository.findByPhoneNumber(person.getPhoneNumber());
        if (existingPerson.isPresent()) {
            if (!existingPerson.get().equals(person)) {
                throw new IllegalArgumentException("Phone number is already registered to another person");
            }
            return existingPerson.get();
        }
        personRepository.save(person);
        return person;
    }

    public synchronized Vehicle registerVehicle(Person person, String registrationNumber) {
        Objects.requireNonNull(person, "person must not be null");
        Objects.requireNonNull(registrationNumber, "registrationNumber must not be null");
        if (registrationNumber.isBlank()) {
            throw new IllegalArgumentException("registrationNumber must not be blank");
        }
        String registration = registrationNumber;
        Person registeredPerson = personRepository.findByPhoneNumber(person.getPhoneNumber())
                .orElseThrow(() -> new IllegalArgumentException("Person is not registered"));
        if (!registeredPerson.equals(person)) {
            throw new IllegalArgumentException("Person details do not match the registered person");
        }

        for (Person existingPerson : personRepository.findAll()) {
            for (Vehicle existingVehicle : existingPerson.getVehicles()) {
                if (existingVehicle.getRegistrationNumber().equalsIgnoreCase(registration)) {
                    if (!existingPerson.equals(registeredPerson)) {
                        throw new IllegalArgumentException("Vehicle registration is already registered to another person");
                    }
                    return existingVehicle;
                }
            }
        }

        Vehicle vehicle = new Vehicle(registration, registeredPerson);
        registeredPerson.addVehicle(vehicle);
        return vehicle;
    }

    /** Validates the complete booking request before persisting its person or vehicle. */
    public synchronized Reservation bookReservation(
            Person person, String registrationNumber, ParkingSpace parkingSpace, Duration plannedDuration) {
        completeExpiredReservationsAt(LocalDateTime.now());
        Objects.requireNonNull(person, "person must not be null");
        Objects.requireNonNull(parkingSpace, "parkingSpace must not be null");
        Objects.requireNonNull(plannedDuration, "plannedDuration must not be null");
        Objects.requireNonNull(registrationNumber, "registrationNumber must not be null");
        if (registrationNumber.isBlank()) {
            throw new IllegalArgumentException("registrationNumber must not be blank");
        }
        if (plannedDuration.isZero() || plannedDuration.isNegative()) {
            throw new IllegalArgumentException("plannedDuration must be positive");
        }
        if (!parkingArea.getSpaces().contains(parkingSpace)) {
            throw new IllegalArgumentException("Parking space does not belong to this parking area");
        }
        if (parkingSpace.getStatus() != SpaceStatus.AVAILABLE) {
            throw new IllegalStateException("Parking space " + parkingSpace.getSpaceId() + " is already occupied");
        }

        Person registeredPerson = personRepository.findByPhoneNumber(person.getPhoneNumber())
                .map(existing -> {
                    if (!existing.equals(person)) {
                        throw new IllegalArgumentException("Phone number is already registered to another person");
                    }
                    return existing;
                })
                .orElse(person);

        Vehicle registeredVehicle = null;
        for (Person existingPerson : personRepository.findAll()) {
            for (Vehicle existingVehicle : existingPerson.getVehicles()) {
                if (existingVehicle.getRegistrationNumber().equalsIgnoreCase(registrationNumber)) {
                    if (!existingPerson.equals(registeredPerson)) {
                        throw new IllegalArgumentException(
                                "Vehicle registration is already registered to another person");
                    }
                    registeredVehicle = existingVehicle;
                    break;
                }
            }
            if (registeredVehicle != null) {
                break;
            }
        }

        LocalDateTime startTime = LocalDateTime.now();
        try {
            startTime.plus(plannedDuration);
        } catch (DateTimeException | ArithmeticException exception) {
            throw new IllegalArgumentException("plannedDuration is too large", exception);
        }

        // All request/business validation is complete before any registration is persisted.
        if (personRepository.findByPhoneNumber(person.getPhoneNumber()).isEmpty()) {
            personRepository.save(registeredPerson);
        }
        if (registeredVehicle == null) {
            registeredVehicle = new Vehicle(registrationNumber, registeredPerson);
            registeredPerson.addVehicle(registeredVehicle);
        }

        Reservation reservation = new Reservation(
                registeredPerson, registeredVehicle, parkingSpace, plannedDuration, startTime);
        reservationRepository.save(reservation);
        return reservation;
    }

    // Create a reservation for a registered person and their vehicle.
    public synchronized Reservation createReservation(
            Person person,
            Vehicle vehicle,
            ParkingSpace parkingSpace,
            Duration plannedDuration) {

        completeExpiredReservationsAt(LocalDateTime.now());

        Objects.requireNonNull(person, "person must not be null");
        Objects.requireNonNull(vehicle, "vehicle must not be null");
        Objects.requireNonNull(parkingSpace, "parkingSpace must not be null");
        Objects.requireNonNull(plannedDuration, "plannedDuration must not be null");

        // The person must already be registered.
        Person registeredPerson = personRepository
                .findByPhoneNumber(person.getPhoneNumber())
                .orElseThrow(() ->
                        new IllegalArgumentException("Person is not registered"));

        // The supplied person must match the registered person.
        if (!registeredPerson.equals(person)) {
            throw new IllegalArgumentException(
                    "Person details do not match the registered person");
        }

        // The vehicle must belong to the reserving person.
        if (!vehicle.getOwner().equals(registeredPerson)
                || registeredPerson.getVehicles().stream().noneMatch(registeredVehicle ->
                        registeredVehicle.getRegistrationNumber().equalsIgnoreCase(vehicle.getRegistrationNumber()))) {
            throw new IllegalArgumentException(
                    "Vehicle does not belong to the registered person");
        }

        // The space must belong to this parking area.
        if (!parkingArea.getSpaces().contains(parkingSpace)) {
            throw new IllegalArgumentException(
                    "Parking space does not belong to this parking area");
        }

        // Reject occupied spaces before creating the reservation.
        if (parkingSpace.getStatus() != SpaceStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Parking space " + parkingSpace.getSpaceId()
                            + " is already occupied");
        }

        // Reservation validates duration and occupies the space.
        Reservation reservation = new Reservation(
                registeredPerson, vehicle, parkingSpace, plannedDuration);

        reservationRepository.save(reservation);

        return reservation;
    }

    // Complete an active reservation when the vehicle leaves.
    public Reservation completeReservation(UUID reservationId) {
        return completeReservation(reservationId, LocalDateTime.now());
    }

    // Overload for deterministic testing with a supplied completion time.
    public Reservation completeReservation(
            UUID reservationId,
            LocalDateTime actualEndTime) {

        Objects.requireNonNull(
                reservationId, "reservationId must not be null");
        Objects.requireNonNull(
                actualEndTime, "actualEndTime must not be null");

        completeExpiredReservationsAt(LocalDateTime.now());

        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Reservation not found"));

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Reservation is not active (current status: "
                            + reservation.getStatus() + ")");
        }

        reservation.complete(actualEndTime);

        // The reservation object is mutable, so saving it keeps
        // the repository contract consistent.
        reservationRepository.save(reservation);

        return reservation;
    }

    public Reservation cancelReservation(UUID reservationId) {
        return cancelReservation(reservationId, LocalDateTime.now());
    }

    public Reservation cancelReservation(UUID reservationId, LocalDateTime cancelledAt) {
        Objects.requireNonNull(reservationId, "reservationId must not be null");
        Objects.requireNonNull(cancelledAt, "cancelledAt must not be null");

        completeExpiredReservationsAt(LocalDateTime.now());

        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        reservation.cancel(cancelledAt);
        reservationRepository.save(reservation);
        return reservation;
    }

    // Return all parking spaces.
    public List<ParkingSpace> getAllSpaces() {
        completeExpiredReservationsAt(LocalDateTime.now());
        return parkingArea.getSpaces();
    }

    // Return only currently available spaces.
    public List<ParkingSpace> getAvailableSpaces() {
        completeExpiredReservationsAt(LocalDateTime.now());
        return parkingArea.getAvailableSpaces();
    }

    // Return reservation history, including active, completed, and cancelled reservations.
    public List<Reservation> getAllReservations() {
        completeExpiredReservationsAt(LocalDateTime.now());
        return reservationRepository.findAll();
    }

    // Return only active reservations.
    public List<Reservation> getActiveReservations() {
        completeExpiredReservationsAt(LocalDateTime.now());
        return reservationRepository.findActive();
    }

    private void completeExpiredReservationsAt(LocalDateTime now) {
        for (Reservation reservation : reservationRepository.findActive()) {
            if (!reservation.getExpectedEndTime().isAfter(now)) {
                reservation.complete(reservation.getExpectedEndTime());
                reservationRepository.save(reservation);
            }
        }
    }
}
