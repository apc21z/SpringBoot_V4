package com.apc21z.proyectv4.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

class JwtServiceTest {

    private static final String SECRET = "test-secret-for-jwt-unit-tests-0123456789";

    @Test
    void generatedTokenContainsUsernameAndValidatesForThatUser() {
        JwtService jwtService = new JwtService(SECRET, 900_000);
        var user = User.withUsername("ana@example.com").password("encoded-password").roles("USER").build();

        String token = jwtService.generateToken(user);

        assertEquals(user.getUsername(), jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));
        assertFalse(jwtService.isTokenValid(token,
                User.withUsername("otra@example.com").password("encoded-password").roles("USER").build()));
    }

    @Test
    void rejectsShortSigningKey() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("too-short", 900_000));
    }
}