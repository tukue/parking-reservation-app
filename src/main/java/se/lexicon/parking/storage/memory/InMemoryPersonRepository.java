package se.lexicon.parking.storage.memory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import se.lexicon.parking.model.Person;
import se.lexicon.parking.storage.PersonRepository;

public class InMemoryPersonRepository implements PersonRepository {
    private final Map<String, Person> peopleByPhoneNumber = new LinkedHashMap<>();

    @Override
    public void save(Person person) {
        Objects.requireNonNull(person, "person must not be null");
        peopleByPhoneNumber.put(person.getPhoneNumber(), person);
    }

    @Override
    public Optional<Person> findByPhoneNumber(String phoneNumber) {
        Objects.requireNonNull(phoneNumber, "phoneNumber must not be null");
        return Optional.ofNullable(peopleByPhoneNumber.get(phoneNumber));
    }

    @Override
    public List<Person> findAll() {
        return List.copyOf(peopleByPhoneNumber.values());
    }
}
