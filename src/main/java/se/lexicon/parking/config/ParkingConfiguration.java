package se.lexicon.parking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import se.lexicon.parking.service.ReservationService;
import se.lexicon.parking.storage.memory.InMemoryParkingStore;

@Configuration
public class ParkingConfiguration {
    @Bean
    InMemoryParkingStore parkingStore() {
        return new InMemoryParkingStore("Lexicon Växjö");
    }

    @Bean
    ReservationService reservationService(InMemoryParkingStore store) {
        return new ReservationService(store.parkingArea(), store.people(), store.reservations());
    }
}
