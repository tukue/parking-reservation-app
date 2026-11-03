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

import se.lexicon.parking.model.ParkingPlace;
import se.lexicon.parking.model.Person;
import se.lexicon.parking.model.ParkingPlaceStatus;
import se.lexicon.parking.service.ReservationService;

@Controller
public class ParkingController {
    private final ReservationService reservationService;

    public ParkingController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/")
    public String home(Model model) {
        var parkingPlaces = reservationService.getAllSpaces();
        var availableParkingPlaces = reservationService.getAvailableSpaces();
        double availabilityPercent = parkingPlaces.isEmpty()
                ? 0.0
                : availableParkingPlaces.size() * 100.0 / parkingPlaces.size();

        model.addAttribute("spaces", parkingPlaces);
        model.addAttribute("availableSpaces", availableParkingPlaces);
        model.addAttribute("availabilityPercent", availabilityPercent);
        model.addAttribute("activeReservations", reservationService.getActiveReservations());
        return "index";
    }

    @PostMapping("/reservations")
    public String createReservation(
            @RequestParam String name,
            @RequestParam String phoneNumber,
            @RequestParam String registrationNumber,
            @RequestParam(required = false) String spotId,
            @RequestParam int durationMinutes,
            RedirectAttributes redirectAttributes) {
        try {
            if (spotId == null || spotId.isBlank()) {
                throw new IllegalArgumentException("Please choose an available parking spot");
            }
            if (durationMinutes < 30 || durationMinutes % 30 != 0) {
                throw new IllegalArgumentException(
                        "Parking duration must be at least 30 minutes and in 30-minute increments");
            }

            ParkingPlace parkingPlace = reservationService.getAllSpaces().stream()
                    .filter(place -> place.getSpotId().equals(spotId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Choose a valid parking spot"));
            if (parkingPlace.getStatus() != ParkingPlaceStatus.AVAILABLE) {
                throw new IllegalStateException("Parking spot " + spotId + " is already occupied");
            }

            var reservation = reservationService.bookReservation(
                    new Person(name, phoneNumber), registrationNumber,
                    parkingPlace, Duration.ofMinutes(durationMinutes));

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
            redirectAttributes.addFlashAttribute("successMessage", "Reservation completed. The parking place is available again.");
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
                    "Reservation cancelled. The parking place is available again.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/";
    }
}
