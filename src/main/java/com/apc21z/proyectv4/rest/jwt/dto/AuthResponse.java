package com.apc21z.proyectv4.rest.jwt.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresIn) {
}