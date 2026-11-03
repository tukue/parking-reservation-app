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
import se.lexicon.parking.model.SpaceStatus;
import se.lexicon.parking.service.ReservationService;

@Controller
public class ParkingController {
    private final ReservationService reservationService;

    public ParkingController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/")
    public String home(Model model) {
        var spaces = reservationService.getAllSpaces();
        var availableSpaces = reservationService.getAvailableSpaces();
        double availabilityPercent = spaces.isEmpty()
                ? 0.0
                : availableSpaces.size() * 100.0 / spaces.size();

        model.addAttribute("spaces", spaces);
        model.addAttribute("availableSpaces", availableSpaces);
        model.addAttribute("availabilityPercent", availabilityPercent);
        model.addAttribute("activeReservations", reservationService.getActiveReservations());
        return "index";
    }

    @PostMapping("/reservations")
    public String createReservation(
            @RequestParam String name,
            @RequestParam String phoneNumber,
            @RequestParam String registrationNumber,
            @RequestParam(required = false) String spaceId,
            @RequestParam int durationMinutes,
            RedirectAttributes redirectAttributes) {
        try {
            if (spaceId == null || spaceId.isBlank()) {
                throw new IllegalArgumentException("Please choose an available parking space");
            }
            if (durationMinutes < 30 || durationMinutes % 30 != 0) {
                throw new IllegalArgumentException(
                        "Parking duration must be at least 30 minutes and in 30-minute increments");
            }

            ParkingSpace parkingSpace = reservationService.getAllSpaces().stream()
                    .filter(space -> space.getSpaceId().equals(spaceId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Choose a valid parking space"));
            if (parkingSpace.getStatus() != SpaceStatus.AVAILABLE) {
                throw new IllegalStateException("Parking space " + spaceId + " is already occupied");
            }

            var reservation = reservationService.bookReservation(
                    new Person(name, phoneNumber), registrationNumber,
                    parkingSpace, Duration.ofMinutes(durationMinutes));

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

    @PostMapping("/reservations/{reservationId}/cancel")
    public String cancelReservation(
            @PathVariable UUID reservationId,
            RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelReservation(reservationId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Reservation cancelled. The space is available again.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/";
    }
}
