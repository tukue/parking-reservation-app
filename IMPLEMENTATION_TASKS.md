# Parking Reservation App — Implementation Tasks

Use these tasks to divide implementation into small, reviewable pieces. Each task should be handled on its own feature branch and include its tests in the same pull request. Agree on public method signatures before parallel work begins.

## 1. Domain model

- [x] **Done** — Add `Person`, `Vehicle`, `ParkingArea`, `ParkingSpace`, and `Reservation` model classes.
- [x] **Done** — Add `SpaceStatus` and `ReservationStatus` enums.
- [x] **Done** — Validate model inputs, reservation ownership, and reservation duration.
- [x] **Done** — Add equality and hash-code behavior for `Person`.
- [x] **Done** — Test person equality and accepting an equivalent owner instance.
- [x] **Done** — Add model tests for invalid construction, reservation completion, duplicate completion, and space availability transitions.

**Tests at this level:** unit tests for valid construction, invalid/null/blank input, expected end-time calculation, active/completed state, and space availability transitions.

## 2. Reservation use cases — completed

**Depends on:** domain model and the repository interfaces from Task 3.

- [x] **Done** — Implement `createReservation(Person, Vehicle, ParkingSpace, Duration)` and `completeReservation(UUID)` use cases.
- [x] **Done** — Reject occupied or foreign parking spaces, unregistered people, mismatched vehicle owners, and already-completed reservations.
- [x] **Done** — Provide `getAllSpaces`, `getAvailableSpaces`, `getAllReservations`, and `getActiveReservations` queries.
- [x] **Done** — Keep use-case rules in the service and save completed reservations through the repository interface.

**Tests at this level:** `ReservationServiceTest` covers successful reservation/completion, occupied and foreign-space rejection, unregistered people, mismatched vehicle ownership, duplicate/unknown reservations, and availability before and after completion.

## 3. Data storage

**Suggested owner:** one teammate. **Depends on:** domain model.

For this learning challenge, the repository interfaces are the storage abstraction. Use **in-memory storage only** in the first version. Do not add a database, database-specific abstractions, or storage dependencies. Data is available while the application is running and is cleared when it stops; database persistence can be added later as a separate extension.

- [x] **Done** — Define `PersonRepository` (`save`, `findByPhoneNumber`, `findAll`) and `ReservationRepository` (`save`, `findById`, `findAll`, `findActive`).
- [x] **Done** — Implement `InMemoryPersonRepository` and `InMemoryReservationRepository` with Java collections; reservations are keyed by UUID.
- [x] **Done** — Keep the `ParkingArea` instance and its 20 `ParkingSpace` objects in one shared `InMemoryParkingStore`.
- [x] **Done** — Create each repository once in `InMemoryParkingStore` and expose it through its interface for Task 2's service to use.
- [x] **Done** — Keep storage operations out of the model classes and user interface.
- [x] **Done** — Ensure reservations can be found by ID and active reservations can be listed.
- [x] **Done** — Keep the storage abstraction limited to the repository interfaces needed by the current use cases.

**Tests at this level:** `InMemoryPersonRepositoryTest`, `InMemoryReservationRepositoryTest`, and `InMemoryParkingStoreTest` cover saving, finding, listing, active-reservation updates, shared repository state, and the 20-space area. No database integration tests are needed for this version.

## 4. User interaction

**Suggested owner:** one teammate. **Depends on:** reservation service.

- [ ] Agree on the initial interface (for example, a console menu or a web API) before implementation.
- [ ] Support registering a person and vehicle.
- [ ] Display spaces and their availability.
- [ ] Allow a person to reserve an available space for a duration and complete the reservation when leaving.
- [ ] Show useful validation and error messages.

**Tests at this level:** interface or controller tests for inputs, outputs, successful actions, and user-visible errors. Keep business-rule tests in the service layer.

## 5. End-to-end integration and release readiness

**Suggested owner:** shared integration task. **Depends on:** service, storage, and interface tasks.

- [ ] Wire the application entry point to the selected interface, service, and storage implementation.
- [ ] Verify the full flow: register → view spaces → reserve → view occupied space → complete reservation → view available space.
- [ ] Update the README with setup, run, and test instructions.
- [ ] Review the UML and domain notes against the implemented behavior.

**Tests at this level:** an end-to-end test of the reservation flow using the selected storage configuration, plus `mvn clean verify` in CI.

## Team workflow

1. Pick one unchecked task and assign one owner.
2. Agree on the API or method signatures other tasks depend on.
3. Implement the behavior and its tests together on a feature branch.
4. Open a focused pull request; request review from a teammate.
5. Merge only after CI passes, then rebase or update other feature branches from `main`.
