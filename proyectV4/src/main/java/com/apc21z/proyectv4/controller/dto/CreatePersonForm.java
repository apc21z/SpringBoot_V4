package com.apc21z.proyectv4.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePersonForm(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Size(max = 120) String profession,
        @NotBlank @Size(max = 300) String skills) {
}