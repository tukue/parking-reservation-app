package se.lexicon.parking.model;

import java.util.Objects;

public class Vehicle {
    private final String registrationNumber;
    private final Person owner;

    public Vehicle(String registrationNumber, Person owner) {
        this.registrationNumber = Person.requireText(registrationNumber, "registrationNumber");
        this.owner = Objects.requireNonNull(owner, "owner must not be null");
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public Person getOwner() {
        return owner;
    }
}
