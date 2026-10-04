package com.apc21z.proyectv4.rest.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookResponseDTO(
    @NotNull Long id,
    @NotBlank String title,
    @NotBlank String author,
    @NotBlank String isbn
) {
}
