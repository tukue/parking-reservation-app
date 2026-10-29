package se.lexicon.parking.storage.memory;

import org.junit.jupiter.api.Test;

import se.lexicon.parking.model.Person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryPersonRepositoryTest {
    @Test
    void savesAndFindsPeopleByPhoneNumber() {
        InMemoryPersonRepository repository = new InMemoryPersonRepository();
        Person person = new Person("Alex Example", "+46123456789");

        repository.save(person);

        assertEquals(person, repository.findByPhoneNumber(person.getPhoneNumber()).orElseThrow());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void returnsEmptyWhenPersonDoesNotExist() {
        InMemoryPersonRepository repository = new InMemoryPersonRepository();

        assertTrue(repository.findByPhoneNumber("+46123456789").isEmpty());
    }

    @Test
    void savingPersonWithSamePhoneNumberUpdatesExistingEntry() {
        InMemoryPersonRepository repository = new InMemoryPersonRepository();
        Person original = new Person("Alex Example", "+46123456789");
        Person updated = new Person("Alex Updated", "+46123456789");
        repository.save(original);

        repository.save(updated);

        assertEquals(updated, repository.findByPhoneNumber("+46123456789").orElseThrow());
        assertEquals(1, repository.findAll().size());
    }
}
