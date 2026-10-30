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
```## 4. System Architecture Diagram

Below is the complete layout diagram mapping our system entities, attributes, and controller relationships:

```mermaid
graph TD
    classDef entity fill:#1e293b,stroke:#38bdf8,stroke-width:2px,color:#f8fafc;
    classDef enum fill:#3f2c5c,stroke:#c084fc,stroke-width:2px,color:#f3e8ff;
    classDef controller fill:#0f172a,stroke:#34d399,stroke-width:2px,color:#f8fafc;

    subgraph Core_Domain_Entities [System Domain Core]
        Person[Entity: Person<br>- name: String<br>- phoneNumber: String]:::entity
        Vehicle[Entity: Vehicle<br>- registrationNumber: String]:::entity
        ParkingArea[Entity: ParkingArea<br>- name: String<br>- capacity: int]:::entity
        ParkingSpace[Entity: ParkingSpace<br>- spaceId: String]:::entity
        Reservation[Entity: Reservation<br>- reservationId: UUID<br>- startTime: LocalDateTime<br>- plannedDuration: Duration<br>- expectedEndTime: LocalDateTime<br>- actualEndTime: LocalDateTime]:::entity
    end

    subgraph State_Enums [State Categories]
        SpaceStatus[Enum: SpaceStatus<br>• AVAILABLE<br>• OCCUPIED]:::enum
        ReservationStatus[Enum: ReservationStatus<br>• ACTIVE<br>• COMPLETED]:::enum
    end

    subgraph Global_Management [Architecture Controller]
        ParkingLot[Controller: ParkingLot<br>+ displayAvailability()<br>+ findAvailableSpace()<br>+ createReservation()<br>+ completeReservation()]:::controller
    end

    Person "1" --> "0..*" Vehicle : owns
    ParkingArea "1" o-- "20" ParkingSpace : contains
    
    Reservation "0..*" --> "1" Person : belongs to
    Reservation "0..*" --> "1" Vehicle : identifies
    Reservation "0..*" --> "1" ParkingSpace : reserves

    ParkingSpace --> SpaceStatus : carries
    Reservation --> ReservationStatus : tracks

    ParkingLot o-- "1" ParkingArea : manages
    ParkingLot o-- "0..*" Reservation : registers
```