package se.lexicon.parking.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ReservationForm {
    @NotBlank(message = "Enter your full name.")
    @Size(max = 100, message = "Name must be 100 characters or fewer.")
    private String name;

    @NotBlank(message = "Enter your phone number.")
    @Size(max = 30, message = "Phone number must be 30 characters or fewer.")
    @Pattern(regexp = "^[+0-9() .-]+$", message = "Enter a valid phone number.")
    private String phoneNumber;

    @NotBlank(message = "Enter the vehicle registration.")
    @Size(max = 20, message = "Vehicle registration must be 20 characters or fewer.")
    @Pattern(regexp = "^[A-Za-z0-9 -]+$", message = "Use only letters, numbers, spaces, or hyphens for registration.")
    private String registrationNumber;

    @NotBlank(message = "Please choose an available parking spot")
    @Pattern(regexp = "^[1-9][0-9]*$", message = "Choose a valid parking spot.")
    private String spotId;

    @NotNull(message = "Enter a parking duration.")
    @Min(value = 30, message = "Parking duration must be at least 30 minutes.")
    private Integer durationMinutes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getSpotId() {
        return spotId;
    }

    public void setSpotId(String spotId) {
        this.spotId = spotId;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
