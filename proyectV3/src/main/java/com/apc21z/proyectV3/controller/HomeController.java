package com.apc21z.proyectV3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.apc21z.proyectV3.service.PersonService;

@Controller
public class HomeController {

    private final PersonService personService;

    public HomeController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/")
    public String getIndex(Model model) {
        model.addAttribute("databaseConnected", personService.isDatabaseConnected());
        return "index";
    }
}
