# Parking Reservation Application - Contributor Workspace

This branch contains the core domain model architecture for the parking reservation system, developed tracking the official project UML specification diagrams.

## 🛠️ Environment Configurations
* **Development Language Version:** Java 25
* **Project Management Blueprint:** Apache Maven 3.8+ / 4.0.0
* **Target Application Architecture:** Object-Oriented Domain Layer

## 📦 Implemented Domain Models
* **`Person`**: Manages customer profiles, storing explicit identification metrics (`name`, `phoneNumber`).
* **`Vehicle`**: Binds automotive metrics directly to unique ownership profiles (`registrationNumber`, `owner`).
* **`SpaceStatus` & `ReservationStatus`**: State enumeration schemas tracking spot availabilities and active time booking parameters.
* **`ParkingArea` & `ParkingSpace`**: Manages spatial distribution lots, slot capacity tracking, and distinct parking slot allocations.
* **`Reservation`**: Governs booking interactions by matching vehicular assets to spot allocations using unique identifiers (`UUID`), standard timestamps (`LocalDateTime`), and planned scheduling periods (`Duration`).

## 🚀 Local Build Verification
The environment compiles cleanly through the integrated Maven lifecycle toolsets inside IntelliJ IDEA:
```bash
mvn clean package
```