package com.apc21z.proyectv4.rest.jwt.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.apc21z.proyectv4.rest.jwt.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    public static final String JWT_COOKIE_NAME = "JWT";

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(JwtProperties jwtProperties) {
        String secret = jwtProperties.getSecret();
        long expirationMillis = jwtProperties.getExpirationMs();

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 bytes");
        }
        if (expirationMillis <= 0) {
            throw new IllegalArgumentException("JWT expiration must be positive");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails.getUsername());
    }

    public String generateToken(String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMillis))
                .signWith(signingKey)
                .compact();
    }

    public ResponseCookie createTokenCookie(String username, boolean secure) {
        return ResponseCookie.from(JWT_COOKIE_NAME, generateToken(username))
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(expirationMillis))
                .build();
    }

    public ResponseCookie clearTokenCookie(boolean secure) {
        return ResponseCookie.from(JWT_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = parseClaims(token);
        return userDetails.getUsername().equals(claims.getSubject())
                && claims.getExpiration().after(new Date());
    }

    public long getExpirationSeconds() {
        return expirationMillis / 1000;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}