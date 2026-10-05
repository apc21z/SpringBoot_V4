package com.apc21z.proyectv4.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.apc21z.proyectv4.rest.model.Person;
import com.apc21z.proyectv4.rest.model.dto.PersonDTO;
import com.apc21z.proyectv4.rest.model.mapper.PersonMapper;
import com.apc21z.proyectv4.rest.service.PersonService;

@RestController
@RequestMapping("/api/people")
public class ApiPeopleController {

    private final PersonService personService;
    private final PersonMapper personMapper;

    public ApiPeopleController(PersonService personService, PersonMapper personMapper) {
        this.personService = personService;
        this.personMapper = personMapper;
    }

    @GetMapping
    public List<PersonDTO> getPeople() {
        return personMapper.toDto(personService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonDTO> getPerson(@PathVariable Long id) {
        return personService.findById(id)
                .map(personMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PersonDTO> createPerson(@Valid @RequestBody PersonDTO personDTO) {
        Person savedPerson = personService.save(personMapper.toEntity(personDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(personMapper.toDto(savedPerson));
    }

    @PutMapping("/{id}")
        public ResponseEntity<PersonDTO> updatePerson(@PathVariable Long id,
            @Valid @RequestBody PersonDTO personDTO) {
        return personService.update(id, personMapper.toEntity(personDTO))
                .map(personMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        if (!personService.deleteById(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}