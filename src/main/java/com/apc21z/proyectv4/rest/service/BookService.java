package com.apc21z.proyectv4.rest.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apc21z.proyectv4.rest.model.Book;
import com.apc21z.proyectv4.rest.repository.BookRepository;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    @Transactional
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    @Transactional
    public Optional<Book> update(Long id, Book changes) {
        return bookRepository.findById(id).map(existingBook -> {
            existingBook.setTitle(changes.getTitle());
            existingBook.setAuthor(changes.getAuthor());
            existingBook.setIsbn(changes.getIsbn());
            existingBook.getPages().clear();
            existingBook.getPages().addAll(changes.getPages());
            return bookRepository.save(existingBook);
        });
    }

    @Transactional
    public boolean deleteById(Long id) {
        if (!bookRepository.existsById(id)) {
            return false;
        }

        bookRepository.deleteById(id);
        return true;
    }
}
