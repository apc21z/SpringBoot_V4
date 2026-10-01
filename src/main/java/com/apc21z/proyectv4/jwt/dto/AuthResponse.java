package com.apc21z.proyectv4.jwt.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresIn) {
}