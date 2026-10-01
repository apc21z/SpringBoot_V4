package com.apc21z.proyectv4.rest.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.apc21z.proyectv4.model.Person;
import com.apc21z.proyectv4.model.Skill;
import com.apc21z.proyectv4.rest.dto.PersonDTO;
import com.apc21z.proyectv4.rest.dto.SkillDTO;

@Mapper(componentModel = "spring")
public interface PersonMapper {

    PersonDTO toDto(Person person);

    List<PersonDTO> toDto(List<Person> people);

    SkillDTO toDto(Skill skill);

    @Mapping(target = "id", ignore = true)
    Person toEntity(PersonDTO personDTO);

    @Mapping(target = "id", ignore = true)
    Skill toEntity(SkillDTO skillDTO);
}