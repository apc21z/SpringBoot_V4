package com.apc21z.proyectv4.rest.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apc21z.proyectv4.rest.model.Book;
import com.apc21z.proyectv4.rest.model.dto.BookCreateDTO;
import com.apc21z.proyectv4.rest.model.dto.BookResponseDTO;
import com.apc21z.proyectv4.rest.model.mapper.BookMapper;
import com.apc21z.proyectv4.rest.service.BookService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/books")
public class ApiBookController {

    private final BookService bookService;
    private final BookMapper bookMapper;

    public ApiBookController(BookService bookService, BookMapper bookMapper) {
        this.bookService = bookService;
        this.bookMapper = bookMapper;
    }

    @GetMapping
    public List<BookResponseDTO> getBooks() {
        return bookMapper.toDto(bookService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getBook(@PathVariable Long id) {
        return bookService.findById(id)
                .map(bookMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody BookCreateDTO bookCreateDTO) {
        Book savedBook = bookService.save(bookMapper.toEntity(bookCreateDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(bookMapper.toDto(savedBook));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDTO> updateBook(@PathVariable Long id,
            @Valid @RequestBody BookCreateDTO bookCreateDTO) {
        return bookService.update(id, bookMapper.toEntity(bookCreateDTO))
                .map(bookMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        if (!bookService.deleteById(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
