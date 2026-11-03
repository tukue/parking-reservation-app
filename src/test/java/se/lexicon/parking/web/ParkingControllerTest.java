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
import se.lexicon.parking.model.SpaceStatus;
import se.lexicon.parking.model.Vehicle;
import se.lexicon.parking.service.ReservationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Make a reservation")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("http-equiv=\"refresh\""))));
    }

    @Test
    void homeRefreshesWhileReservationsAreActive() throws Exception {
        Person person = new Person("Alex Example", "+46123456789");
        Reservation activeReservation = new Reservation(person, new Vehicle("ABC123", person),
                new ParkingSpace("1"), Duration.ofHours(1));
        ParkingSpace space = new ParkingSpace("2");
        when(reservationService.getAllSpaces()).thenReturn(List.of(space));
        when(reservationService.getAvailableSpaces()).thenReturn(List.of(space));
        when(reservationService.getActiveReservations()).thenReturn(List.of(activeReservation));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "http-equiv=\"refresh\" content=\"30\"")));
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
        Reservation reservation = new Reservation(person, new Vehicle("ABC123", person), new ParkingSpace("2"),
                Duration.ofMinutes(30), LocalDateTime.of(2026, 10, 29, 10, 0));
        when(reservationService.getAllSpaces()).thenReturn(List.of(space));
        when(reservationService.bookReservation(any(Person.class), eq("ABC123"), eq(space), any(Duration.class)))
                .thenReturn(reservation);

        mockMvc.perform(post("/reservations")
                        .param("name", "Alex Example")
                        .param("phoneNumber", "+46123456789")
                        .param("registrationNumber", "ABC123")
                        .param("spaceId", "1")
                        .param("durationMinutes", "30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(reservationService).bookReservation(any(Person.class), eq("ABC123"), eq(space),
                eq(Duration.ofMinutes(30)));
    }

    @Test
    void reservationFailureReturnsUserFacingError() throws Exception {
        ParkingSpace space = new ParkingSpace("1");
        when(reservationService.getAllSpaces()).thenReturn(List.of(space));
        doThrow(new IllegalStateException("Parking space 1 is already occupied"))
                .when(reservationService)
                .bookReservation(any(Person.class), eq("ABC123"), any(ParkingSpace.class), any(Duration.class));

        mockMvc.perform(post("/reservations")
                        .param("name", "Alex Example")
                        .param("phoneNumber", "+46123456789")
                        .param("registrationNumber", "ABC123")
                        .param("spaceId", "1")
                        .param("durationMinutes", "60"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Parking space 1 is already occupied"));
    }

    @Test
    void occupiedSpaceIsRejectedBeforeRegisteringPersonOrVehicle() throws Exception {
        ParkingSpace occupiedSpace = new ParkingSpace("1");
        Person existingPerson = new Person("Existing Person", "+46123456789");
        new Reservation(existingPerson, new Vehicle("XYZ789", existingPerson), occupiedSpace,
                Duration.ofHours(1));
        assertEquals(SpaceStatus.OCCUPIED, occupiedSpace.getStatus());
        when(reservationService.getAllSpaces()).thenReturn(List.of(occupiedSpace));

        mockMvc.perform(post("/reservations")
                        .param("name", "Alex Example")
                        .param("phoneNumber", "+46123456789")
                        .param("registrationNumber", "ABC123")
                        .param("spaceId", "1")
                        .param("durationMinutes", "60"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Parking space 1 is already occupied"));

        verify(reservationService).getAllSpaces();
        org.mockito.Mockito.verifyNoMoreInteractions(reservationService);
    }

    @Test
    void unknownSpaceIsRejectedBeforeRegisteringPersonOrVehicle() throws Exception {
        when(reservationService.getAllSpaces()).thenReturn(List.of(new ParkingSpace("1")));

        mockMvc.perform(post("/reservations")
                        .param("name", "Alex Example")
                        .param("phoneNumber", "+46123456789")
                        .param("registrationNumber", "ABC123")
                        .param("spaceId", "999")
                        .param("durationMinutes", "60"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Choose a valid parking space"));

        verify(reservationService).getAllSpaces();
        org.mockito.Mockito.verifyNoMoreInteractions(reservationService);
    }

    @Test
    void missingSpaceSelectionReturnsUserFacingErrorInsteadOfBadRequest() throws Exception {
        mockMvc.perform(post("/reservations")
                        .param("name", "Alex Example")
                        .param("phoneNumber", "+46123456789")
                        .param("registrationNumber", "ABC123")
                        .param("durationMinutes", "60"))
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
                        .param("durationMinutes", "60"))
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

    @Test
    void cancellationFormCancelsTheReservation() throws Exception {
        UUID reservationId = UUID.randomUUID();

        mockMvc.perform(post("/reservations/{reservationId}/cancel", reservationId))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage",
                        "Reservation cancelled. The space is available again."));

        verify(reservationService).cancelReservation(reservationId);
    }
}
