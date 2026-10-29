package se.lexicon.parking.web;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import se.lexicon.parking.model.ParkingSpace;
import se.lexicon.parking.model.Person;
import se.lexicon.parking.model.Reservation;
import se.lexicon.parking.model.Vehicle;
import se.lexicon.parking.service.ReservationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ParkingController.class)
class ParkingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void homeShowsSpacesAndAvailability() throws Exception {
        ParkingSpace space = new ParkingSpace("1");
        when(reservationService.getAllSpaces()).thenReturn(List.of(space));
        when(reservationService.getAvailableSpaces()).thenReturn(List.of(space));
        when(reservationService.getActiveReservations()).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("spaces", List.of(space)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Parking spaces")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Make a reservation")));
    }

    @Test
    void homeRendersWhenThereAreNoSpaces() throws Exception {
        when(reservationService.getAllSpaces()).thenReturn(List.of());
        when(reservationService.getAvailableSpaces()).thenReturn(List.of());
        when(reservationService.getActiveReservations()).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("availabilityPercent", 0.0))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("spaces available")));
    }

    @Test
    void reservationFormRegistersPersonAndCreatesReservation() throws Exception {
        Person person = new Person("Alex Example", "+46123456789");
        ParkingSpace space = new ParkingSpace("1");
        Reservation reservation = new Reservation(person, new Vehicle("ABC123", person), space,
                Duration.ofHours(2), LocalDateTime.of(2026, 10, 29, 10, 0));
        when(reservationService.registerPerson(any(Person.class))).thenReturn(person);
        when(reservationService.getAllSpaces()).thenReturn(List.of(space));
        when(reservationService.createReservation(eq(person), any(Vehicle.class), eq(space), any(Duration.class)))
                .thenReturn(reservation);

        mockMvc.perform(post("/reservations")
                        .param("name", "Alex Example")
                        .param("phoneNumber", "+46123456789")
                        .param("registrationNumber", "ABC123")
                        .param("spaceId", "1")
                        .param("durationHours", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(reservationService).registerPerson(any(Person.class));
        verify(reservationService).createReservation(eq(person), any(Vehicle.class), eq(space),
                eq(Duration.ofHours(2)));
    }

    @Test
    void reservationFailureReturnsUserFacingError() throws Exception {
        Person person = new Person("Alex Example", "+46123456789");
        ParkingSpace space = new ParkingSpace("1");
        when(reservationService.registerPerson(any(Person.class))).thenReturn(person);
        when(reservationService.getAllSpaces()).thenReturn(List.of(space));
        doThrow(new IllegalStateException("Parking space 1 is already occupied"))
                .when(reservationService)
                .createReservation(any(Person.class), any(Vehicle.class), any(ParkingSpace.class),
                        any(Duration.class));

        mockMvc.perform(post("/reservations")
                        .param("name", "Alex Example")
                        .param("phoneNumber", "+46123456789")
                        .param("registrationNumber", "ABC123")
                        .param("spaceId", "1")
                        .param("durationHours", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Parking space 1 is already occupied"));
    }

    @Test
    void missingSpaceSelectionReturnsUserFacingErrorInsteadOfBadRequest() throws Exception {
        mockMvc.perform(post("/reservations")
                        .param("name", "Alex Example")
                        .param("phoneNumber", "+46123456789")
                        .param("registrationNumber", "ABC123")
                        .param("durationHours", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Please choose an available parking space"));

        verifyNoInteractions(reservationService);
    }

    @Test
    void blankSpaceSelectionReturnsUserFacingError() throws Exception {
        mockMvc.perform(post("/reservations")
                        .param("name", "Alex Example")
                        .param("phoneNumber", "+46123456789")
                        .param("registrationNumber", "ABC123")
                        .param("spaceId", " ")
                        .param("durationHours", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Please choose an available parking space"));

        verifyNoInteractions(reservationService);
    }

    @Test
    void completionFormCompletesTheReservation() throws Exception {
        UUID reservationId = UUID.randomUUID();

        mockMvc.perform(post("/reservations/{reservationId}/complete", reservationId))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));

        verify(reservationService).completeReservation(reservationId);
    }
}
