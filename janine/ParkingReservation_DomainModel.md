# Parking Reservation System — Domain Model

## Purpose

The system manages 20 parking spaces at the Lexicon building in Växjö. It keeps track of people, vehicles, parking reservations, and which parking spaces are available or occupied.

## Main Domain Concepts

- **Person:** A person who wants to park at the Lexicon building. The person provides a name and phone number.
- **Vehicle:** The vehicle being parked. It is identified by its registration number.
- **Parking Space:** One of the 20 parking spaces. A space can be available or occupied.
- **Reservation:** A record of a parking space being reserved. It has a unique ID, a start time, a planned duration, an expected end time, and a completion status.
- ## Relationships

- A **Person** makes a **Reservation**.
- A **Reservation** is associated with a **Vehicle**.
- A **Reservation** reserves one **Parking Space**.
- A **Parking Space** can be reserved again after the current reservation is completed.
- A **Reservation** is completed when the person leaves, and the parking space becomes available again.