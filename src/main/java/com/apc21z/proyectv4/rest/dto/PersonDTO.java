package com.apc21z.proyectv4.rest.dto;

import java.util.List;

public record PersonDTO(Long id, String firstName, String lastName, String profession, List<SkillDTO> skills) {
}