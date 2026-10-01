package com.apc21z.proyectv4.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import com.apc21z.proyectv4.service.PersonService;
import com.apc21z.proyectv4.service.UserAccountService;

@Controller
public class IndexController {

    private final PersonService personService;
    private final UserAccountService userAccountService;

    public IndexController(PersonService personService, UserAccountService userAccountService) {
        this.personService = personService;
        this.userAccountService = userAccountService;
    }

    @GetMapping("/")
    public String getIndex(Model model) {
        model.addAttribute("databaseConnected", personService.isDatabaseConnected());
        return "index";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {
        try {
            userAccountService.register(email, password);
            redirectAttributes.addFlashAttribute("successMessage", "Cuenta creada correctamente. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
            return "redirect:/register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboardPage(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("admin", authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
        return "dashboard";
    }

    @GetMapping("/admin")
    public String adminPage(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "admin";
    }
}