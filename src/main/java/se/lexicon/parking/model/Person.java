package se.lexicon.parking.model;

import java.util.Objects;

public class Person {
    private final String name;
    private final String phoneNumber;

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
