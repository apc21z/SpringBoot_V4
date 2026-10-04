package com.apc21z.proyectv4.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import com.apc21z.proyectv4.security.jwt.config.JwtProperties;
import com.apc21z.proyectv4.security.jwt.service.JwtService;

class JwtServiceTest {

    private static final String SECRET = "test-secret-for-jwt-unit-tests-0123456789";

    private static JwtService jwtServiceFor(String secret) {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(secret);
        jwtProperties.setExpirationMs(900_000);
        return new JwtService(jwtProperties);
    }

    @Test
    void generatedTokenContainsUsernameAndValidatesForThatUser() {
        JwtService jwtService = jwtServiceFor(SECRET);
        var user = User.withUsername("ana@example.com").password("encoded-password").roles("USER").build();

        String token = jwtService.generateToken(user);

        assertEquals(user.getUsername(), jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));
        assertFalse(jwtService.isTokenValid(token,
                User.withUsername("otra@example.com").password("encoded-password").roles("USER").build()));
    }

    @Test
    void rejectsShortSigningKey() {
        assertThrows(IllegalArgumentException.class, () -> jwtServiceFor("too-short"));
    }
}