package com.apc21z.proyectv4.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apc21z.proyectv4.model.Person;

public interface PersonRepository extends JpaRepository<Person, Long> {
}