package se.lexicon.parking.storage.memory;

import org.junit.jupiter.api.Test;

import se.lexicon.parking.model.ParkingArea;
import se.lexicon.parking.model.Person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class InMemoryParkingStoreTest {
    @Test
    void retainsSharedRepositoriesAndParkingArea() {
        InMemoryParkingStore store = new InMemoryParkingStore("Lexicon Växjö");
        Person person = new Person("Alex Example", "+46123456789");

        store.people().save(person);

        assertSame(store.people(), store.people());
        assertEquals(person, store.people().findByPhoneNumber(person.getPhoneNumber()).orElseThrow());
        assertSame(store.parkingArea(), store.parkingArea());
        assertEquals(ParkingArea.SPACE_COUNT, store.parkingArea().getSpaces().size());
    }
}
