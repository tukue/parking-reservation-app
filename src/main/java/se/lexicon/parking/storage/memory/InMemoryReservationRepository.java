package se.lexicon.parking.storage.memory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import se.lexicon.parking.model.Reservation;
import se.lexicon.parking.model.ReservationStatus;
import se.lexicon.parking.storage.ReservationRepository;

public class InMemoryReservationRepository implements ReservationRepository {
    private final Map<UUID, Reservation> reservationsById = new LinkedHashMap<>();

    @Override
    public void save(Reservation reservation) {
        Objects.requireNonNull(reservation, "reservation must not be null");
        reservationsById.put(reservation.getReservationId(), reservation);
    }

    @Override
    public Optional<Reservation> findById(UUID reservationId) {
        Objects.requireNonNull(reservationId, "reservationId must not be null");
        return Optional.ofNullable(reservationsById.get(reservationId));
    }

    @Override
    public List<Reservation> findAll() {
        return List.copyOf(reservationsById.values());
    }

    @Override
    public List<Reservation> findActive() {
        return reservationsById.values().stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.ACTIVE)
                .toList();
    }
}
