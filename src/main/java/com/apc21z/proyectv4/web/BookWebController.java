package com.apc21z.proyectv4.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import com.apc21z.proyectv4.rest.model.Book;
import com.apc21z.proyectv4.rest.service.BookService;

@Controller
@RequestMapping("/books")
public class BookWebController {

    private static final int PAGE_SIZE = 5;

    private final BookService bookService;

    public BookWebController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String booksPage(@RequestParam(defaultValue = "0") int page, Model model) {
        int pageNumber = Math.max(0, page);
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Page<Book> bookPage = bookService.findAll(PageRequest.of(pageNumber, PAGE_SIZE, sort));
        if (bookPage.getTotalPages() > 0 && pageNumber >= bookPage.getTotalPages()) {
            pageNumber = bookPage.getTotalPages() - 1;
            Pageable lastPage = PageRequest.of(pageNumber, PAGE_SIZE, sort);
            bookPage = bookService.findAll(lastPage);
        }

        model.addAttribute("bookPage", bookPage);
        return "books";
    }

    @GetMapping("/test")
    public String booksApiTesterPage() {
        return "books-test";
    }
}
