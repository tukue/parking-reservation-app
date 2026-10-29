package se.lexicon.parking.storage;

import java.util.List;
import java.util.Optional;

import se.lexicon.parking.model.Person;

public interface PersonRepository {
    void save(Person person);

    Optional<Person> findByPhoneNumber(String phoneNumber);

    List<Person> findAll();
}
