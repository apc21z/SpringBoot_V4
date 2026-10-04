package com.apc21z.proyectv4.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apc21z.proyectv4.rest.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
    
}
