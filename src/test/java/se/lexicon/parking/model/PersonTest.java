package se.lexicon.parking.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PersonTest {
    @Test
    void peopleWithSameNameAndPhoneNumberAreEqual() {
        Person first = new Person("Alex Example", "+46123456789");
        Person second = new Person("Alex Example", "+46123456789");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void peopleWithDifferentDetailsAreNotEqual() {
        Person first = new Person("Alex Example", "+46123456789");
        Person second = new Person("Alex Example", "+46987654321");

        assertNotEquals(first, second);
    }
}
