package com.apc21z.proyectv4.rest.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apc21z.proyectv4.rest.model.Person;

public interface PersonRepository extends JpaRepository<Person, Long> {

	@Query(value = "select distinct person from Person person left join person.skills personSkill "
			+ "where (:skill is null or lower(personSkill.name) like concat('%', lower(:skill), '%'))",
			countQuery = "select count(distinct person.id) from Person person left join person.skills personSkill "
					+ "where (:skill is null or lower(personSkill.name) like concat('%', lower(:skill), '%'))")
	Page<Person> findPageBySkill(@Param("skill") String skill, Pageable pageable);
}