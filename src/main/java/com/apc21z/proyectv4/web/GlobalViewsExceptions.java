package com.apc21z.proyectv4.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackages = "com.apc21z.proyectv4.web")
public class GlobalViewsExceptions {

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception exception) {
        ModelAndView modelAndView = new ModelAndView("error/500");
        modelAndView.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return modelAndView;
    }
}
