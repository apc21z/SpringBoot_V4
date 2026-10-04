package com.apc21z.proyectv4.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.apc21z.proyectv4.controller.dto.CreatePersonForm;
import com.apc21z.proyectv4.rest.model.Person;
import com.apc21z.proyectv4.rest.model.Skill;
import com.apc21z.proyectv4.rest.service.PersonService;

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
        addPeopleToModel(model, skill);
        model.addAttribute("status", status);
        model.addAttribute("personForm", new CreatePersonForm("", "", "", ""));
        return "people";
    }

    @PostMapping
    public String createPerson(@Valid @ModelAttribute("personForm") CreatePersonForm form,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return showInvalidForm(model);
        }

        List<Skill> personSkills = new ArrayList<>();
        for (String skill : form.skills().split(",")) {
            String trimmedSkill = skill.trim();
            if (!trimmedSkill.isEmpty()) {
                personSkills.add(new Skill(trimmedSkill));
            }
        }

        if (personSkills.isEmpty()) {
            bindingResult.rejectValue("skills", "skills.empty", "Indica al menos una skill.");
            return showInvalidForm(model);
        }

        personService.save(new Person(form.firstName().trim(), form.lastName().trim(), form.profession().trim(),
                personSkills));
        return "redirect:/people?status=created";
    }

    private String showInvalidForm(Model model) {
        addPeopleToModel(model, "");
        model.addAttribute("status", "invalid");
        model.addAttribute("skillFilter", "");
        return "people";
    }

    private void addPeopleToModel(Model model, String skill) {
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
    }
}