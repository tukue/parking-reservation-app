# Parking Availability and Reservations — Domain Concepts

## Purpose

The system manages the 20 parking spaces at the Lexicon building in Växjö. It lets students, staff, and visitors view availability, reserve an available space, and release it when they leave.

## Core concepts

### Person

A student, staff member, or visitor who registers and makes reservations.

 person attributes
- Name
- Phone number

### Vehicle

A vehicle associated with a person.
 vehicle attributes
- Vehicle registration number

Each vehicle is retained as part of its owner's registration and can be used for
multiple reservations. A registration number can belong to only one person.

### Parking lot

The Lexicon parking lot, containing 20 numbered parking places.

### Parking space

A parking place in the lot that can be available or occupied. Each place has a spot identifier so it can be selected and displayed.

### Reservation

A record that a person and their vehicle are using a particular parking space for a period of time.

reservation attributes

- Unique reservation identifier
- Start time
- Planned parking duration
- Expected end time
- Actual end time, recorded when the person leaves
- Status (active, completed, or cancelled)

## Relationships

- A parking lot contains 20 parking places.
- A person can be associated with one or more vehicles.
- A person can make multiple reservations over time.
- Each reservation belongs to one person and identifies one vehicle.
- Each reservation uses exactly one parking space.
- A parking space can have many reservations over time, but at most one active reservation at a time.

## Domain rules

1. Only an available space can be reserved.
2. Creating a reservation records its start time and planned duration, calculates the expected end time, and marks the selected space occupied.
3. Completing a reservation records when the person leaves and makes the space available again.
4. The availability display reflects whether each parking place is currently available or occupied.
5. When the system next processes availability or a reservation action, an active reservation at or past its expected end time is completed, its expected end time is recorded as its end time, and its space is released.
6. A person may complete a reservation early when leaving; cancelling an active reservation also releases the space.
