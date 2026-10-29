# Parking Reservation App — Implementation Tasks

Use these tasks to divide implementation into small, reviewable pieces. Each task should be handled on its own feature branch and include its tests in the same pull request. Agree on public method signatures before parallel work begins.

## 1. Domain model

- [x] Add `Person`, `Vehicle`, `ParkingArea`, `ParkingSpace`, and `Reservation` model classes.
- [x] Add `SpaceStatus` and `ReservationStatus` enums.
- [x] Validate model inputs, reservation ownership, and reservation duration.
- [x] Add equality and hash-code behavior for `Person`.
- [x] Test person equality and accepting an equivalent owner instance.
- [ ] Add model tests for invalid construction, reservation completion, duplicate completion, and space availability transitions.

**Tests at this level:** unit tests for valid construction, invalid/null/blank input, expected end-time calculation, active/completed state, and space availability transitions.

## 2. Reservation use cases

**Suggested owner:** one teammate. **Depends on:** domain model.

- [ ] Create a `ReservationService` to reserve an available space and complete an active reservation.
- [ ] Prevent reserving an occupied space or completing an already-completed reservation.
- [ ] Provide queries for all spaces and currently available spaces.
- [ ] Keep reservation creation/completion rules in the service rather than the UI.

**Tests at this level:** service unit tests using the in-memory model. Cover successful reservation/completion, occupied-space rejection, invalid person/vehicle ownership, and availability before and after completion.

## 3. Data storage

**Suggested owner:** one teammate. **Depends on:** service interfaces and team decision on storage.

- [ ] Agree whether the first version uses in-memory storage or a database.
- [ ] Define repository interfaces for people, spaces, and reservations.
- [ ] Implement the selected storage adapter without placing storage logic in domain classes.
- [ ] Ensure reservation IDs are unique and active reservations can be retrieved.

**Tests at this level:** repository tests for saving, looking up, and retrieving reservations and spaces. If a database is selected, run integration tests against the configured test database.

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
