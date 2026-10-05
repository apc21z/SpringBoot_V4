package com.apc21z.proyectv4.rest.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookResponseDTO(
    @NotNull Long id,
    @NotBlank @Size(max = 255) String title,
    @NotBlank @Size(max = 255) String author,
    @NotBlank @Size(max = 255) String isbn
) {
}
