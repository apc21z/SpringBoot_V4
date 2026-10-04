package com.apc21z.proyectv4.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/books")
public class BookWebController {

    @GetMapping
    public String booksPage() {
        return "books";
    }

    @GetMapping("/test")
    public String booksApiTesterPage() {
        return "books-test";
    }
}
