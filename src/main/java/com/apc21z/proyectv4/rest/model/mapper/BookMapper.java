package com.apc21z.proyectv4.rest.model.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.apc21z.proyectv4.rest.model.Book;
import com.apc21z.proyectv4.rest.model.BookPage;
import com.apc21z.proyectv4.rest.model.dto.BookCreateDTO;
import com.apc21z.proyectv4.rest.model.dto.BookPageDTO;
import com.apc21z.proyectv4.rest.model.dto.BookResponseDTO;

@Mapper(componentModel = "spring")
public interface BookMapper {

    BookResponseDTO toDto(Book book);

    List<BookResponseDTO> toDto(List<Book> books);

    BookPageDTO toDto(BookPage page);

    @Mapping(target = "id", ignore = true)
    Book toEntity(BookCreateDTO bookCreateDTO);

    @Mapping(target = "id", ignore = true)
    BookPage toEntity(BookPageDTO bookPageDTO);
}
