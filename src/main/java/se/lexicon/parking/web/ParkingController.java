package se.lexicon.parking.web;

import java.time.Duration;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import se.lexicon.parking.model.ParkingSpace;
import se.lexicon.parking.model.Person;
import se.lexicon.parking.model.Reservation;
import se.lexicon.parking.model.Vehicle;
import se.lexicon.parking.service.ReservationService;

@Controller
public class ParkingController {
    private final ReservationService reservationService;

    public ParkingController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("spaces", reservationService.getAllSpaces());
        model.addAttribute("availableSpaces", reservationService.getAvailableSpaces());
        model.addAttribute("activeReservations", reservationService.getActiveReservations());
        return "index";
    }

    @PostMapping("/reservations")
    public String createReservation(
            @RequestParam String name,
            @RequestParam String phoneNumber,
            @RequestParam String registrationNumber,
            @RequestParam String spaceId,
            @RequestParam int durationHours,
            RedirectAttributes redirectAttributes) {
        try {
            if (durationHours <= 0) {
                throw new IllegalArgumentException("Parking duration must be at least one hour");
            }

            Person person = reservationService.registerPerson(new Person(name, phoneNumber));
            Vehicle vehicle = new Vehicle(registrationNumber, person);
            ParkingSpace parkingSpace = reservationService.getAllSpaces().stream()
                    .filter(space -> space.getSpaceId().equals(spaceId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Choose a valid parking space"));
            Reservation reservation = reservationService.createReservation(
                    person, vehicle, parkingSpace, Duration.ofHours(durationHours));

            redirectAttributes.addFlashAttribute("successMessage",
                    "Reservation " + reservation.getReservationId() + " created. Expected end: "
                            + reservation.getExpectedEndTime());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/reservations/{reservationId}/complete")
    public String completeReservation(
            @PathVariable UUID reservationId,
            RedirectAttributes redirectAttributes) {
        try {
            reservationService.completeReservation(reservationId);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation completed. The space is available again.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/";
    }
}
