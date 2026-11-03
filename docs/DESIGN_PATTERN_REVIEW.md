# Design Patterns Used in the Parking App

This document explains the patterns and structures already used in the course project, where they appear, and how they help the parking app. It describes the current implementation rather than proposing new patterns.

## Model–View–Controller (MVC)

The web application separates request handling, page rendering, and domain behavior:

- **Controller:** `ParkingController` handles page requests and booking, completion, and cancellation form submissions.
- **View:** `src/main/resources/templates/index.html` renders parking-space availability, the booking form, and active reservations using Thymeleaf.
- **Model/domain:** `Person`, `Vehicle`, `ParkingSpace`, and `Reservation` represent the parking concepts and their rules.

For example, a booking form submission is handled by the controller, which calls the service; the service returns data that the controller or view uses to show the result.

## Repository pattern

`PersonRepository` and `ReservationRepository` define the storage operations the application needs. `InMemoryPersonRepository` and `InMemoryReservationRepository` implement those interfaces using Java collections.

`ReservationService` depends on the interfaces rather than directly manipulating the repository maps. This keeps storage details separate from booking rules and allows a different repository implementation to be added later.

## Service Layer

`ReservationService` provides application operations such as `bookReservation`, `completeReservation`, `cancelReservation`, and the availability queries. The controller delegates to these operations instead of implementing the reservation workflow itself.

The service coordinates the use case: it validates the person, vehicle, duration, and parking space, then registers the person and vehicle and creates the reservation. Booking methods are synchronized to prevent overlapping operations on the shared in-memory state within this application instance.

## Dependency Injection

Spring creates and shares the application components:

- `ParkingConfiguration` creates the shared in-memory store and `ReservationService` beans.
- `ParkingController` receives `ReservationService` through its constructor.
- The service receives the parking area and repository interfaces through its constructor.

This makes dependencies explicit and ensures requests use the same in-memory parking state.

## Domain Model

The domain classes combine parking data with rules that protect valid state changes:

- `ParkingSpace` starts as available and changes availability when a reservation occupies or releases it.
- `Reservation` calculates its expected end time and allows completion or cancellation only while its status is `ACTIVE`.
- `Person` retains one or more registered vehicles.

The service checks when reservations have reached their expected end time and completes them, releasing their spaces. The UI refreshes while reservations are active so it can display the updated availability.

## Patterns not used

The app does not currently use a separate **State pattern**, **Strategy pattern**, or **Factory pattern**. Reservation states are few and are handled with an enum and guarded methods. There is one parking flow and no pricing policy to vary, so additional pattern classes would add complexity without solving a current problem.
