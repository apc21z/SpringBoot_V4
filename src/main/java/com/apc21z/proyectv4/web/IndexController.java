package com.apc21z.proyectv4.web;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.apc21z.proyectv4.rest.service.PersonService;
import com.apc21z.proyectv4.rest.service.UserAccountService;
import com.apc21z.proyectv4.security.jwt.dto.RegisterRequest;

import org.springframework.web.server.ResponseStatusException;

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
    public String registerPage(Model model) {
        model.addAttribute("registerForm", new RegisterRequest("", ""));
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterRequest request,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userAccountService.register(request.email(), request.password());
            redirectAttributes.addFlashAttribute("successMessage", "Cuenta creada correctamente. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (ResponseStatusException ex) {
            model.addAttribute("errorMessage", ex.getReason());
            return "register";
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