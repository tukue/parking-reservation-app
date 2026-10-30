
package se.lexicon.parking.service;

import java.time.Duration;
import java.time.LocalDateTime;
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

    // Create a reservation for a registered person and their vehicle.
    public Reservation createReservation(
            Person person,
            Vehicle vehicle,
            ParkingSpace parkingSpace,
            Duration plannedDuration) {

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
        if (!vehicle.getOwner().equals(registeredPerson)) {
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

    public Person registerPerson(Person person) {
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

    // Return all parking spaces.
    public List<ParkingSpace> getAllSpaces() {
        return parkingArea.getSpaces();
    }

    // Return only currently available spaces.
    public List<ParkingSpace> getAvailableSpaces() {
        return parkingArea.getAvailableSpaces();
    }

    // Return all reservations, active and completed.
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    // Return only active reservations.
    public List<Reservation> getActiveReservations() {
        return reservationRepository.findActive();
    }
}
