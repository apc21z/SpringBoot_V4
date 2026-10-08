package com.apc21z.proyectv4.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.apc21z.proyectv4.rest.model.Person;
import com.apc21z.proyectv4.web.dto.CreatePersonForm;
import com.apc21z.proyectv4.rest.model.Skill;
import com.apc21z.proyectv4.rest.service.PersonService;

@Controller
@RequestMapping("/people")
public class PeopleController {

    private static final int PEOPLE_PAGE_SIZE = 5;

    private final PersonService personService;

    public PeopleController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public String getPeoplePage(@RequestParam(required = false, defaultValue = "") String skill,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page, Model model) {
        addPeopleToModel(model, skill, page);
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
        addPeopleToModel(model, "", 0);
        model.addAttribute("status", "invalid");
        model.addAttribute("skillFilter", "");
        return "people";
    }

    private void addPeopleToModel(Model model, String skill, int requestedPage) {
        String filter = skill.trim();
        String normalizedFilter = filter.toLowerCase(Locale.ROOT);
        PageRequest pageRequest = PageRequest.of(Math.max(0, requestedPage), PEOPLE_PAGE_SIZE,
            Sort.by(Sort.Direction.ASC, "id"));
        Page<Person> peoplePage = personService.findPageBySkill(
            normalizedFilter.isEmpty() ? null : normalizedFilter, pageRequest);
        if (peoplePage.getTotalPages() > 0 && pageRequest.getPageNumber() >= peoplePage.getTotalPages()) {
            pageRequest = PageRequest.of(peoplePage.getTotalPages() - 1, PEOPLE_PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, "id"));
            peoplePage = personService.findPageBySkill(normalizedFilter.isEmpty() ? null : normalizedFilter,
                pageRequest);
        }

        model.addAttribute("people", peoplePage.getContent());
        model.addAttribute("peopleTotal", peoplePage.getTotalElements());
        model.addAttribute("currentPage", peoplePage.getNumber());
        model.addAttribute("totalPages", peoplePage.getTotalPages());
        model.addAttribute("firstPerson", peoplePage.isEmpty() ? 0
            : peoplePage.getNumber() * peoplePage.getSize() + 1);
        model.addAttribute("lastPerson", peoplePage.getNumber() * peoplePage.getSize()
            + peoplePage.getNumberOfElements());
        model.addAttribute("skillFilter", filter);
    }
}