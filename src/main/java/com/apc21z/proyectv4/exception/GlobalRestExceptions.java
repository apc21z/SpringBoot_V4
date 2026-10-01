package com.apc21z.proyectv4.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice 
public class GlobalRestExceptions {
    
    public String handleException(Exception exception) {
        return "Error: " + exception.getMessage();
    }

}
