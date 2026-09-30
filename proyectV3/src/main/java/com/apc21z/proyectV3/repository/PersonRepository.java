package com.apc21z.proyectV3.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apc21z.proyectV3.model.Person;

public interface PersonRepository extends JpaRepository<Person, Long> {
}