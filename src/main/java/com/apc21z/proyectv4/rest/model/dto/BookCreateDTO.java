package com.apc21z.proyectv4.rest.model.dto;

import jakarta.validation.constraints.NotBlank;

public record BookCreateDTO(
    @NotBlank String title,
    @NotBlank String author,
    @NotBlank String isbn
) {
}