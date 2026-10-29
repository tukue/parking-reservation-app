package se.lexicon.parking;

public class Vehicle {
    private String registrationNumber;
    private Person owner; // This links the vehicle to the Person class you made earlier

    // Constructor to create a vehicle
    public Vehicle(String registrationNumber, Person owner) {
        this.registrationNumber = registrationNumber;
        this.owner = owner;
    }

    // Getters and Setters
    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }
}