package se.lexicon.parking.storage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import se.lexicon.parking.model.Reservation;

public interface ReservationRepository {
    void save(Reservation reservation);

    Optional<Reservation> findById(UUID reservationId);

    List<Reservation> findAll();

    List<Reservation> findActive();
}
