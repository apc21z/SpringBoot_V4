package com.apc21z.proyectV3.service;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apc21z.proyectV3.model.Person;
import com.apc21z.proyectV3.repository.PersonRepository;

@Service
@Transactional(readOnly = true)
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    public Optional<Person> findById(Long id) {
        return personRepository.findById(id);
    }

    @Transactional
    public Person save(Person person) {
        return personRepository.save(person);
    }

    @Transactional
    public Optional<Person> update(Long id, Person person) {
        return personRepository.findById(id).map(existingPerson -> {
            existingPerson.setFirstName(person.getFirstName());
            existingPerson.setLastName(person.getLastName());
            existingPerson.setProfession(person.getProfession());
            existingPerson.setSkills(person.getSkills());
            return personRepository.save(existingPerson);
        });
    }

    @Transactional
    public boolean deleteById(Long id) {
        if (!personRepository.existsById(id)) {
            return false;
        }

        personRepository.deleteById(id);
        return true;
    }

    public boolean isDatabaseConnected() {
        try {
            personRepository.count();
            return true;
        } catch (DataAccessException exception) {
            return false;
        }
    }
}