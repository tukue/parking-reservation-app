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

- [x] **Done** — Implement `createReservation(Person, Vehicle, ParkingSpace, Duration)`, `completeReservation(UUID)`, and `cancelReservation(UUID)` use cases.
- [x] **Done** — Reject occupied or foreign parking spaces, unregistered people, mismatched vehicle owners, and inactive reservations being completed or cancelled.
- [x] **Done** — Provide `getAllSpaces`, `getAvailableSpaces`, `getAllReservations`, and `getActiveReservations` queries.
- [x] **Done** — Keep use-case rules in the service and save completed and cancelled reservations through the repository interface.

**Tests at this level:** `ReservationServiceTest` covers successful reservation/completion/cancellation, occupied and foreign-space rejection, unregistered people, mismatched vehicle ownership, duplicate/unknown reservations, and availability before and after completion or cancellation.

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

## 4. User interface (UI) — completed

**Depends on:** reservation service. Implemented as a Spring Boot MVC application with Thymeleaf templates.

- [x] **Done** — Display all parking spaces and current availability.
- [x] **Done** — Provide a reservation form for person, vehicle, available space, and duration; register the person during booking.
- [x] **Done** — Show the reservation ID and expected end time after booking.
- [x] **Done** — Allow a reservation to be completed and refresh the space availability.
- [x] **Done** — Allow an active reservation to be cancelled, show confirmation, and refresh space availability.
- [x] **Done** — Show validation and reservation errors in the UI.

**Tests at this level:** `ParkingControllerTest` covers the availability page, reservation, completion and cancellation submissions, and user-visible reservation errors. Business rules remain covered by service tests.

## 5. End-to-end integration and release readiness

**Suggested owner:** shared integration task. **Depends on:** service, storage, and interface tasks.

- [x] **Done** — Wire the Spring Boot entry point to the UI, service, and shared in-memory storage.
- [x] **Done** — Verify the full flow: register → view spaces → reserve → view occupied space → complete reservation → view available space.
- [x] **Done** — Add and run `ParkingFlowIntegrationTest` against the Spring application and in-memory storage.
- [x] **Done** — Update the README with setup, run, and test instructions.
- [x] **Done** — Review the UML and domain notes against the implemented behavior.

**Tests at this level:** `ParkingFlowIntegrationTest` exercises the full browser/controller/service/in-memory-storage flow. CI runs `mvn clean test` and packages the application.

## Future product roadmap

**Proposed future scope:** The current learning version intentionally uses in-memory storage and does not charge for parking. Resolve the product decisions in [the design document](docs/FUTURE_PRODUCT_IMPROVEMENTS.md) before beginning the payment phases.

### Phase 1: Persistent storage

- [ ] Select the relational database and migration tool; document local setup and environment-based credentials.
- [ ] Add schema migrations for people, vehicles, parking areas/spaces, and reservations.
- [ ] Implement database-backed repository adapters while retaining in-memory adapters for unit tests.
- [ ] Persist reservation and space state changes atomically; prevent more than one active reservation for a space under concurrent requests.
- [ ] Add repository integration tests and verify reservations and availability survive an application restart.

**Acceptance:** Existing reservation flows pass against database-backed repositories, test data is isolated, migrations run from a clean database, and restart verification confirms data is retained.

### Phase 2: Reservation cancellation — unpaid flow implemented

- [x] **Done** — Add a `CANCELLED` reservation status and a cancellation operation for active reservations.
- [x] **Done** — Record the cancellation time, release the parking space, and keep cancelled reservations in history.
- [x] **Done** — Add a cancel action for active reservations in the UI with clear success and error messages.
- [x] **Done** — Reject cancellation of completed or already-cancelled reservations; verify cancellation makes the space available for another reservation.
- [x] **Done** — Add model, service, controller, and end-to-end tests for successful and rejected cancellation flows.

**Acceptance:** An active unpaid reservation can be cancelled from the UI; its space becomes available, the reservation remains in history as `CANCELLED`, and a second cancellation is rejected. Paid reservation/refund behavior remains future scope until a refund policy is approved.

### Phase 3: Pricing and estimates

- [ ] Agree on billing unit, currency, rounding, minimum/maximum charge, free periods, and rate effective dates.
- [ ] Add a rate model that supports free and paid spaces; snapshot the applicable rate on each reservation.
- [ ] Calculate and display an estimate before reservation confirmation and validate the amount server-side.
- [ ] Add unit tests for free parking, rate changes, duration boundaries, and monetary rounding.

**Acceptance:** The displayed estimate matches the server-calculated estimate and existing reservations retain their original rate snapshot after rate updates.

### Phase 4: Payment-provider sandbox flow

- [ ] Select a payment provider and collection timing; define pending-payment and space-hold behavior.
- [ ] Add payment records and explicit payment statuses linked to reservations.
- [ ] Add a provider-neutral payment service and a hosted-checkout adapter configured for sandbox mode.
- [ ] Verify signed webhooks and make payment creation and webhook handling idempotent.
- [ ] Show pending, successful, and failed payment states; provide a receipt or transaction reference after confirmation.
- [ ] Add tests for success, failure, timeout, duplicate requests, forged/invalid webhooks, and provider retries.

**Acceptance:** No reservation is presented as paid until the provider confirms it; retrying a request or webhook cannot duplicate a charge or payment record.

### Phase 5: Operational readiness

- [ ] Define payment reconciliation, support lookup, and approved refund procedures.
- [ ] Add structured logs and monitoring for payment outcomes without logging secrets or sensitive card data.
- [ ] Document privacy, retention, backup/restore, and production-secret handling.
- [ ] Complete end-to-end sandbox testing and a product/security review before enabling live payments.

**Acceptance:** Operators can investigate a reservation/payment by reference, restore persistent data from backup, and distinguish pending, failed, and successful transactions.

## Team workflow

1. Pick one unchecked task and assign one owner.
2. Agree on the API or method signatures other tasks depend on.
3. Implement the behavior and its tests together on a feature branch.
4. Open a focused pull request; request review from a teammate.
5. Merge only after CI passes, then rebase or update other feature branches from `main`.

## Git workflow best practices

- Start from an up-to-date `main` and create a branch for each focused task, using names such as `feature/reservation-service` or `fix/space-availability`.
- Avoid committing directly to `main`. Keep each pull request focused and small enough for teammates to review.
- Coordinate ownership of files and agree on shared method signatures before parallel changes to related features.
- Make small commits with descriptive messages. Stage only the files for the task.
- Do not commit generated output (`target/`), IDE settings (`.idea/`), local environment files, or secrets.
- Before pushing a feature branch or opening a pull request, run `git fetch origin` and `git rebase origin/main` to replay your work on the latest `main`; then run `mvn clean verify` and check the CI result.
- Resolve conflicts carefully and communicate before rewriting shared branch history; avoid force-pushing branches other teammates use.
- Merge through a reviewed pull request after required checks pass, then update local `main` before starting the next task.
