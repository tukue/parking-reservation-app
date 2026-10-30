package se.lexicon.parking.model;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

public class Person {
    private final String name;
    private final String phoneNumber;

    public static final List<Person> people = new ArrayList<>();


    public Person(String name, String phoneNumber) {
        this.name = requireText(name, "name");
        this.phoneNumber = requireText(phoneNumber, "phoneNumber");
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void displayPerson(){
        IO.println("Name: " + name);
        IO.println("Phone Number: " + phoneNumber);
    }

    public static void addPerson(Person person) {
        people.add(person);
    }

    public static List<Person> getPeople() {
        return List.copyOf(people);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Person person)) {
            return false;
        }
        return name.equals(person.name) && phoneNumber.equals(person.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, phoneNumber);
    }

    static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
