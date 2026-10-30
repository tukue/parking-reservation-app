package se.lexicon.parking.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import se.lexicon.parking.model.ReservationStatus;
import se.lexicon.parking.model.SpaceStatus;
import se.lexicon.parking.storage.memory.InMemoryParkingStore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ParkingFlowIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryParkingStore store;

    @Test
    void visitorCanRegisterReserveAndCompleteParking() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("20</span> spaces available")));

        mockMvc.perform(post("/reservations")
                        .param("name", "Taylor Visitor")
                        .param("phoneNumber", "+46123456789")
                        .param("registrationNumber", "ABC123")
                        .param("spaceId", "1")
                        .param("durationHours", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(flash().attributeExists("successMessage"));

        var allReservations = store.reservations().findAll();
        assertEquals(1, allReservations.size(), "Expected exactly one reservation after POST");
        var reservation = allReservations.getFirst();
        var parkingSpace = reservation.getParkingSpace();
        assertEquals("Taylor Visitor", reservation.getPerson().getName());
        assertEquals("ABC123", reservation.getVehicle().getRegistrationNumber());
        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());
        assertEquals(SpaceStatus.OCCUPIED, parkingSpace.getStatus());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("19</span> spaces available")))
                .andExpect(content().string(containsString("ABC123")));

        mockMvc.perform(post("/reservations/{reservationId}/complete", reservation.getReservationId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));

        assertEquals(ReservationStatus.COMPLETED, reservation.getStatus());
        assertEquals(SpaceStatus.AVAILABLE, parkingSpace.getStatus());
        assertTrue(store.reservations().findActive().isEmpty());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("20</span> spaces available")))
                .andExpect(content().string(containsString("There are no active reservations.")));
    }

    @Test
    void visitorCanCancelReservationAndReleaseSpace() throws Exception {
        mockMvc.perform(post("/reservations")
                        .param("name", "Taylor Visitor")
                        .param("phoneNumber", "+46123456789")
                        .param("registrationNumber", "ABC123")
                        .param("spaceId", "1")
                        .param("durationHours", "2"))
                .andExpect(status().is3xxRedirection());

        var reservation = store.reservations().findAll().getFirst();
        var parkingSpace = reservation.getParkingSpace();
        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());
        assertEquals(SpaceStatus.OCCUPIED, parkingSpace.getStatus());

        mockMvc.perform(post("/reservations/{reservationId}/cancel", reservation.getReservationId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        assertEquals(SpaceStatus.AVAILABLE, parkingSpace.getStatus());
        assertTrue(store.reservations().findActive().isEmpty());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("20</span> spaces available")));
    }
}
