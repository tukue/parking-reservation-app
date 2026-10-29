# Parking Availability and Reservations — Domain Concepts

## Purpose

The system manages the 20 parking spaces at the Lexicon building in Växjö. It lets students, staff, and visitors view availability, reserve an available space, and release it when they leave.

## Core concepts

### Person

A student, staff member, or visitor who registers and makes reservations.

- Name
- Phone number

### Vehicle

A vehicle associated with a person.

- Vehicle registration number

### Parking area

The Lexicon parking area, containing 20 parking spaces.

### Parking space

A space in the parking area that can be available or occupied. Each space has an identifier so it can be selected and displayed.

### Reservation

A record that a person and their vehicle are using a particular parking space for a period of time.

- Unique reservation identifier
- Start time
- Planned parking duration
- Expected end time
- Actual end time, recorded when the person leaves
- Status (active or completed)

## Relationships

- A parking area contains 20 parking spaces.
- A person can be associated with one or more vehicles.
- A person can make multiple reservations over time.
- Each reservation belongs to one person and identifies one vehicle.
- Each reservation uses exactly one parking space.
- A parking space can have many reservations over time, but at most one active reservation at a time.

## Domain rules

1. Only an available space can be reserved.
2. Creating a reservation records its start time and planned duration, calculates the expected end time, and marks the selected space occupied.
3. Completing a reservation records when the person leaves and makes the space available again.
4. The availability display reflects whether each parking space is currently available or occupied.
