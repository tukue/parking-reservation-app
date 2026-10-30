package se.lexicon.parking;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;

import se.lexicon.parking.model.ParkingSpace;
import se.lexicon.parking.model.Person;
import se.lexicon.parking.model.Reservation;
import se.lexicon.parking.model.Vehicle;
import se.lexicon.parking.service.ReservationService;
import se.lexicon.parking.storage.memory.InMemoryParkingStore;

public class App {
    static void main() {
        InMemoryParkingStore store = new InMemoryParkingStore("Lexicon Växjö");
        ReservationService service = new ReservationService(
                store.parkingArea(),
                store.people(),
                store.reservations()
        );

        Map<String, Person> registeredPeople = new HashMap<>();
        Map<String, Vehicle> registeredVehicles = new HashMap<>();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println();
                System.out.println("===== PARKING RESERVATION MENU =====");
                System.out.println("1. Register person");
                System.out.println("2. Register vehicle");
                System.out.println("3. Display parking spaces");
                System.out.println("4. Create reservation");
                System.out.println("5. Complete reservation (vehicle leaves)");
                System.out.println("6. Exit");

                System.out.print("Choose an option: ");
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> registerPerson(scanner, service, registeredPeople);
                    case "2" -> registerVehicle(scanner, service, registeredPeople, registeredVehicles);
                    case "3" -> displayParkingSpaces(service);
                    case "4" -> createReservation(scanner, service, registeredPeople, registeredVehicles);
                    case "5" -> completeReservation(scanner, service);
                    case "6" -> {
                        System.out.println("Exiting parking reservation system...");
                        return;
                    }
                    default -> System.out.println("Invalid option. Please try again.");
                }
            }
        }
    }

    private static void registerPerson(Scanner scanner, ReservationService service,
                                       Map<String, Person> registeredPeople) {
        try {
            System.out.print("Enter person name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter phone number: ");
            String phoneNumber = scanner.nextLine().trim();

            Person person = service.registerPerson(new Person(name, phoneNumber));
            registeredPeople.put(phoneNumber, person);

            System.out.println("Person registered successfully: " + person.getName());
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private static void registerVehicle(Scanner scanner, ReservationService service,
                                        Map<String, Person> registeredPeople,
                                        Map<String, Vehicle> registeredVehicles) {
        try {
            System.out.print("Enter phone number for owner: ");
            String phoneNumber = scanner.nextLine().trim();

            Person owner = registeredPeople.get(phoneNumber);

            if (owner == null) {
                System.out.println("Person not found. Please register the person first.");
                return;
            }

            System.out.print("Enter vehicle registration number: ");
            String registrationNumber = scanner.nextLine().trim();

            Vehicle vehicle = new Vehicle(registrationNumber, owner);
            registeredVehicles.put(registrationNumber, vehicle);

            System.out.println("Vehicle registered successfully for " + owner.getName());
        } catch (IllegalArgumentException e) {
            System.out.println("Vehicle registration failed: " + e.getMessage());
        }
    }

    private static void displayParkingSpaces(ReservationService service) {
        List<ParkingSpace> spaces = service.getAllSpaces();

        System.out.println("Parking spaces:");
        for (ParkingSpace space : spaces) {
            System.out.println("Space " + space.getSpaceId() + " -> " + space.getStatus());
        }
    }

    private static void createReservation(Scanner scanner, ReservationService service,
                                          Map<String, Person> registeredPeople,
                                          Map<String, Vehicle> registeredVehicles) {
        try {
            System.out.print("Enter phone number: ");
            String phoneNumber = scanner.nextLine().trim();

            Person person = registeredPeople.get(phoneNumber);
            if (person == null) {
                System.out.println("Person not registered. Please register first.");
                return;
            }

            System.out.print("Enter vehicle registration number: ");
            String registrationNumber = scanner.nextLine().trim();

            Vehicle vehicle = registeredVehicles.get(registrationNumber);
            if (vehicle == null) {
                System.out.println("Vehicle not registered. Please register the vehicle first.");
                return;
            }

            System.out.print("Enter parking space id: ");
            String spaceId = scanner.nextLine().trim();

            System.out.print("Enter duration in hours: ");
            int hours = Integer.parseInt(scanner.nextLine().trim());

            ParkingSpace space = service.getAllSpaces().stream()
                    .filter(current -> Objects.equals(current.getSpaceId(), spaceId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Parking space not found"));

            Reservation reservation = service.createReservation(
                    person,
                    vehicle,
                    space,
                    Duration.ofHours(hours)
            );

            System.out.println("Reservation created successfully.");
            System.out.println("Reservation ID: " + reservation.getReservationId());
            System.out.println("Expected end time: " + reservation.getExpectedEndTime());

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number of hours.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Reservation failed: " + e.getMessage());
        }
    }

    private static void completeReservation(Scanner scanner, ReservationService service) {
        try {
            System.out.print("Enter reservation ID: ");
            String idText = scanner.nextLine().trim();

            UUID reservationId = UUID.fromString(idText);
            Reservation reservation = service.completeReservation(reservationId);

            System.out.println("Reservation completed successfully.");
            System.out.println("Completed reservation ID: " + reservation.getReservationId());

        } catch (IllegalArgumentException e) {
            System.out.println("Could not complete reservation: " + e.getMessage());
        }
    }
}