package com.apc21z.proyectv4.security.jwt.dto;

import java.util.List;

public record UserAccountDTO(Long id, String email, List<String> roles) {

    public UserAccountDTO {
        roles = List.copyOf(roles);
    }
}