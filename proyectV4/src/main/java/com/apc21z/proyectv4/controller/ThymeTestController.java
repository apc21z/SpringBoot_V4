package com.apc21z.proyectv4.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.apc21z.proyectv4.model.Person;
import com.apc21z.proyectv4.model.Skill;
import com.apc21z.proyectv4.service.PersonService;

@Controller
@RequestMapping("/thymeTest")
@SessionAttributes("person")
public class ThymeTestController {

    private final PersonService personService;

    public ThymeTestController(PersonService personService) {
        this.personService = personService;
    }

    @ModelAttribute("person")
    public Person createPerson() {
        return new Person("John", "Doe", "Software Engineer",
                List.of(new Skill("Java"), new Skill("Python")));
    }

    @GetMapping
    public String getThymeTest(@RequestParam(required = false, defaultValue = "") String profession, Model model) {
        String filter = profession.trim();
        List<Person> people = personService.findAll().stream()
                .filter(person -> person.getProfession().toLowerCase(Locale.ROOT)
                        .contains(filter.toLowerCase(Locale.ROOT)))
                .toList();
        model.addAttribute("people", people);
        model.addAttribute("professionFilter", filter);
        return "thymeTest";
    }

    @GetMapping("/person/{firstName}")
    public String getPersonByPathVariable(@PathVariable String firstName, Model model) {
        model.addAttribute("people", personService.findAll());
        model.addAttribute("professionFilter", "");
        model.addAttribute("pathVariableValue", firstName);
        return "thymeTest";
    }

    @PostMapping("/skills")
    public String addSkill(@ModelAttribute("person") Person person, @RequestParam String skill) {
        if (skill != null && !skill.isBlank()) {
            List<Skill> skills = new ArrayList<>(person.getSkills());
            skills.add(new Skill(skill.trim()));
            person.setSkills(skills);
        }
        return "redirect:/thymeTest";
    }
}