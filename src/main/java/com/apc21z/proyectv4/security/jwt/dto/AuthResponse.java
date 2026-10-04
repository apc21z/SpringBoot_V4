package com.apc21z.proyectv4.security.jwt.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresIn) {
}