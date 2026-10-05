package com.apc21z.proyectv4.rest.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookPageDTO(
        Long id,
        @NotBlank @Size(max = 255) String title,
        @NotBlank String text) {
}