package com.apc21z.proyectV3.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.apc21z.proyectV3.model.Person;
import com.apc21z.proyectV3.model.Skill;
import com.apc21z.proyectV3.service.PersonService;

@Controller
@RequestMapping("/people")
public class PeopleController {

    private final PersonService personService;

    public PeopleController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public String getPeoplePage(@RequestParam(required = false, defaultValue = "") String skill,
            @RequestParam(required = false) String status, Model model) {
        String filter = skill.trim();
        String normalizedFilter = filter.toLowerCase(Locale.ROOT);
        List<Person> people = personService.findAll().stream()
                .filter(person -> normalizedFilter.isEmpty()
                        || person.getSkills() != null && person.getSkills().stream()
                                .anyMatch(personSkill -> personSkill.getName() != null
                                        && personSkill.getName().toLowerCase(Locale.ROOT).contains(normalizedFilter)))
                .toList();
        model.addAttribute("people", people);
        model.addAttribute("skillFilter", filter);
        model.addAttribute("status", status);
        return "people";
    }

    @PostMapping
    public String createPerson(@RequestParam String firstName, @RequestParam String lastName,
            @RequestParam String profession, @RequestParam String skills) {
        List<Skill> personSkills = new ArrayList<>();
        for (String skill : skills.split(",")) {
            String trimmedSkill = skill.trim();
            if (!trimmedSkill.isEmpty()) {
                personSkills.add(new Skill(trimmedSkill));
            }
        }

        if (firstName.isBlank() || lastName.isBlank() || profession.isBlank() || personSkills.isEmpty()) {
            return "redirect:/people?status=invalid";
        }

        personService.save(new Person(firstName.trim(), lastName.trim(), profession.trim(), personSkills));
        return "redirect:/people?status=created";
    }
}