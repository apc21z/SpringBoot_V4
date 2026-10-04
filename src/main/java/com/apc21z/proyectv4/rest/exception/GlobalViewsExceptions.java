package com.apc21z.proyectv4.rest.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice 
public class GlobalViewsExceptions {
    
    public String handleException(Exception exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

}
