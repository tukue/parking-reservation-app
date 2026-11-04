package se.lexicon.parking.web;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import se.lexicon.parking.model.ParkingPlace;
import se.lexicon.parking.model.Person;
import se.lexicon.parking.model.ParkingPlaceStatus;
import se.lexicon.parking.service.ReservationService;

@Controller
public class ParkingController {
    private static final List<String> VALIDATION_FIELD_ORDER = List.of(
            "name", "phoneNumber", "registrationNumber", "spotId", "durationMinutes");

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
            @Valid @ModelAttribute("reservationForm") ReservationForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .sorted(Comparator
                            .comparingInt((FieldError error) -> {
                                int order = VALIDATION_FIELD_ORDER.indexOf(error.getField());
                                return order < 0 ? Integer.MAX_VALUE : order;
                            })
                            .thenComparing(FieldError::getDefaultMessage,
                                    Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(FieldError::getDefaultMessage)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.joining(" "));
            if (errorMessage.isBlank()) {
                errorMessage = "Please correct the booking details and try again.";
            }
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            redirectAttributes.addFlashAttribute("reservationForm", form);
            return "redirect:/";
        }

        try {
            if (form.getDurationMinutes() < ReservationForm.MIN_DURATION_MINUTES
                    || form.getDurationMinutes() % ReservationForm.DURATION_INCREMENT_MINUTES != 0) {
                throw new IllegalArgumentException(ReservationForm.INVALID_DURATION_MESSAGE);
            }

            ParkingPlace parkingPlace = reservationService.getAllSpaces().stream()
                    .filter(place -> place.getSpotId().equals(form.getSpotId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Choose a valid parking spot"));
            if (parkingPlace.getStatus() != ParkingPlaceStatus.AVAILABLE) {
                throw new IllegalStateException("Parking spot " + form.getSpotId() + " is already occupied");
            }

            var reservation = reservationService.bookReservation(
                    new Person(form.getName(), form.getPhoneNumber()), form.getRegistrationNumber(),
                    parkingPlace, Duration.ofMinutes(form.getDurationMinutes()));

            redirectAttributes.addFlashAttribute("successMessage",
                    "Reservation " + reservation.getReservationId() + " created. Expected end: "
                            + reservation.getExpectedEndTime());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            redirectAttributes.addFlashAttribute("reservationForm", form);
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
