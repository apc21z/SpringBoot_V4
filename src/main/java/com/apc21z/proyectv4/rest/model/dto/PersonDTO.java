package com.apc21z.proyectv4.rest.model.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PersonDTO(
	Long id,
	@NotBlank @Size(max = 255) String firstName,
	@NotBlank @Size(max = 255) String lastName,
	@Size(max = 255) String profession,
	@Valid List<SkillDTO> skills) {
}