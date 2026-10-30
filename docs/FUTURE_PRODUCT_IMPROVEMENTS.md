# Future Product Improvements: Persistent Data and Paid Parking

**Status:** Proposed roadmap; not part of the current in-memory learning version.
**Scope:** Preserve parking data across application restarts and support payment for parking spaces or periods that are not free.

## Product context

The current app lets people register, view spaces, create reservations, and complete them. It does not yet let users cancel an active reservation. Its repositories use in-memory collections, so data is lost when the application stops. The current domain also has no rate or payment concepts. These improvements should be delivered in stages: persistence first, cancellation next, then pricing and payment.

## Goals

- Keep people, vehicles, reservations, and parking-space records across restarts.
- Keep availability correct when multiple requests reserve or release spaces.
- Clearly show whether parking is free or paid, the applicable rate, and an estimated charge before a user confirms.
- Record payment outcomes and provide a traceable receipt for paid parking.
- Keep payment-provider details out of the core reservation rules and never store card credentials.

## Out of scope for the first release

- Multiple parking locations, subscriptions, discounts, permits, enforcement, or refunds initiated by an administrator.
- Taking direct custody of card data.
- Final pricing policy or provider selection; these require a product decision before implementation.

## Proposed user experience

1. The user selects an available parking space and supplies the vehicle and planned duration as today.
2. The app displays whether the space is free or paid, the pricing basis, and an estimated total. The user reviews the amount before confirming.
3. The app creates the reservation and, for paid parking, starts a payment flow with the selected payment provider.
4. The app marks payment as confirmed only after a verified provider confirmation. A pending or failed payment is shown clearly and must not be presented as paid.
5. On completion, the app records the actual end time and calculates the final amount according to the approved pricing policy. It records the payment result and shows a receipt or an outstanding-payment action.
6. A user can cancel an active reservation. The app records when it was cancelled, marks it `CANCELLED`, and releases the space. Completed or already-cancelled reservations cannot be cancelled again.

The exact point at which a paid reservation becomes active (before payment, after successful payment, or after a temporary hold) is a product decision. The implementation must avoid leaving a space indefinitely blocked by an abandoned payment.
For the current unpaid flow, cancellation releases the space and preserves the reservation in history. Once payments are introduced, the product must define whether cancellation triggers a full refund, partial refund, or no refund; a reservation must not be shown as refunded until the payment provider confirms it.

## Proposed domain and persistence model

Use a relational database with migrations. PostgreSQL is the proposed production database; use the same database family for integration tests where practical, or an explicitly configured test database. Keep domain rules in services and preserve repository interfaces so in-memory implementations remain useful for fast unit tests.

Proposed persistent records:

- **Person** and **Vehicle:** stable identifiers and the existing contact/registration data; apply access controls and retention rules to personal data.
- **ParkingArea** and **ParkingSpace:** stable identifiers and current configuration. Avoid relying on process-local object identity.
- **Reservation:** person, vehicle, space, start and expected/actual end times, status (`ACTIVE`, `COMPLETED`, or `CANCELLED`), cancellation time when applicable, and a snapshot of the pricing rule used when the reservation was created.
- **ParkingRate:** amount, currency, billing unit, effective dates, and whether the space is free. Rate changes must not silently rewrite existing reservations.
- **Payment:** reservation, amount in the currency's smallest unit (for example öre for SEK), currency, provider, provider reference, status, and timestamps. Store no card number or security code.

Persist reservation and space state changes transactionally. Enforce at most one active reservation per space in the database as well as in service validation, so concurrent requests cannot reserve the same space. Use migrations for schema changes and keep credentials in environment-based configuration, not source control.

## Payment integration

Implement a provider-neutral payment interface in the application and a separate adapter for the selected payment service. A hosted checkout flow is preferred for a first release. Provider webhooks—not the browser return page—are the source of truth for asynchronous payment status.

- Use an idempotency key for payment creation and make webhook processing safe to retry.
- Verify webhook signatures before applying updates.
- Model payment states explicitly, such as `PENDING`, `SUCCEEDED`, `FAILED`, and `REFUNDED` if refunds are later approved.
- Store provider IDs and status history needed for support and reconciliation; do not store payment credentials.
- Keep payment secrets outside source control and use sandbox/test mode until the flow is reviewed.
- Make payment failure recoverable: show a clear status and next action, and release any temporary space hold according to a defined timeout.

The payment provider, payment timing, taxes/receipts requirements, and pricing/rounding rules are open product decisions. Do not hard-code a provider-specific policy into the reservation model.

## Success measures

- Reservations and availability survive an application restart.
- Concurrent requests cannot create two active reservations for one space.
- A user can cancel an active reservation and release its space; terminal reservations cannot be cancelled again.
- Users see the applicable rate and estimate before confirming paid parking.
- A payment is shown as successful only after verified provider confirmation; retries do not create duplicate charges.
- Support can trace a reservation to its payment outcome without access to card data.

## Risks and mitigations

| Risk | Mitigation |
| --- | --- |
| Concurrent reservations oversell a space | Transactional writes and a database constraint for one active reservation per space. |
| Payment succeeds but the browser never returns | Treat verified webhooks as authoritative and provide a way to reload status. |
| Duplicate callbacks or retries cause duplicate charges | Provider idempotency keys and idempotent webhook handling. |
| Rate changes make old charges unclear | Store the rate/pricing snapshot on each reservation. |
| Personal or payment data is exposed | Minimize stored personal data; never store card details; keep secrets out of Git. |

## Decisions needed before implementation

1. Which payment methods and provider are needed for the target users and region?
2. Is payment collected before parking, at session completion, or by another flow?
3. What is the rate model (hourly, fixed session, rounding, minimum, maximum, and free periods)?
4. What should happen when a payment fails or remains pending, and how long may a space be held?
5. What refund policy applies when a paid reservation is cancelled?
6. What receipt, tax, privacy, and data-retention requirements apply?

## Roadmap

See [the implementation roadmap](../IMPLEMENTATION_TASKS.md#future-product-roadmap) for phased tasks and acceptance checks.
