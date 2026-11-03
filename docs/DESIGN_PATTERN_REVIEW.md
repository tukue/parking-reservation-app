# Parking App — Design Pattern Review

This document compares the patterns already present in the course project with small, practical improvements that could be made as the app grows. The goal is to keep the code understandable; a pattern should be added only when it solves a current problem.

## Patterns already used

| Pattern / structure | Where it appears | How it helps |
|---|---|---|
| **Model–View–Controller (MVC)** | `ParkingController`, Thymeleaf `index.html`, and the domain model | Keeps HTTP handling, page rendering, and parking rules in separate parts of the app. |
| **Repository** | `PersonRepository`, `ReservationRepository`, and their in-memory implementations | Separates service logic from the current storage mechanism. The repository interfaces can later have database implementations. |
| **Service Layer** | `ReservationService` | Provides application operations such as booking, completion, cancellation, and availability queries. The controller calls these operations rather than implementing the rules itself. |
| **Dependency Injection** | `ParkingConfiguration` and the controller constructor | Supplies the shared store and service to the application instead of making each class construct its own dependencies. |
| **Domain Model** | `Reservation`, `ParkingSpace`, `Person`, and `Vehicle` | Keeps core data and state rules with the domain objects. For example, a reservation can only be completed or cancelled while active. |

## Practical improvements to consider

### 1. Add a `Clock` dependency for time-based behavior

**Problem:** Reservation creation and expiry use `LocalDateTime.now()`. Tests that need to exercise the exact expiry boundary must construct dates relative to the real clock.

**Small improvement:** Inject Java's `java.time.Clock` into `ReservationService` and use `LocalDateTime.now(clock)`. Tests can use a fixed clock, while the application uses `Clock.systemDefaultZone()`.

**Pattern/principle:** Dependency injection and dependency inversion. `Clock` is a small time-source abstraction provided by Java; no custom time framework is needed.

**Benefit:** Expiry and booking tests become deterministic without sleeping or depending on the time at which a test happens to run.

### 2. Add a vehicle repository when vehicle lookup becomes a storage concern

**Current approach:** A person's retained vehicles live in the `Person` object, and `ReservationService` scans registered people to ensure a registration number is not owned by someone else.

**Possible improvement:** If the app gains persistent storage or many users, introduce a `VehicleRepository` with operations such as `findByRegistrationNumber` and `save`. The service could then check registration ownership directly rather than scanning all people.

**Pattern:** Repository. This extends the existing storage abstraction and is most useful when the storage or query needs grow. For the current in-memory course app, the scan is small and understandable, so a new repository is optional.

### 3. Use Strategy for pricing or interchangeable parking policies

**Future use:** If the app adds different prices, discounts, or rules for different space types, define a small policy interface (for example, `ParkingRatePolicy`) and provide implementations for each pricing rule.

**Pattern:** Strategy. The service can use a policy without containing a growing set of pricing conditionals.

**When to add it:** Only when there is more than one real pricing or policy rule. There is no current need to add strategy classes to the unpaid, single-rate flow.

### 4. Keep the current reservation status enum for now

The `ACTIVE`, `COMPLETED`, and `CANCELLED` states are represented clearly by `ReservationStatus`, with transition checks in `Reservation`. A full **State pattern** would add classes and indirection for only a few simple transitions. Reconsider it only if each state gains substantially different behavior.

## Concurrency and persistence note

`ReservationService` currently synchronizes booking operations to protect the shared in-memory state inside one running application. If storage later moves to a database or the app runs in multiple instances, Java `synchronized` will not coordinate those instances. At that point, use a database transaction and an appropriate database constraint or lock to prevent two bookings for the same space.

## Recommended order

1. Keep the existing MVC, service, repository, and domain-model boundaries.
2. Consider injecting `Clock` next, because expiry already makes time central to the behavior and it improves test determinism.
3. Add a vehicle repository or pricing strategy only when persistence or product requirements justify them.
4. Avoid adding a State pattern, generic factories, or other abstractions solely for pattern practice; prefer the smallest design that explains the current rules.
