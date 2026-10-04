package com.apc21z.proyectv4.security.jwt.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apc21z.proyectv4.rest.service.UserAccountService;
import com.apc21z.proyectv4.security.jwt.dto.LoginRequest;
import com.apc21z.proyectv4.security.jwt.dto.RegisterRequest;
import com.apc21z.proyectv4.security.jwt.service.JwtService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserAccountService userAccountService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, UserAccountService userAccountService,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userAccountService = userAccountService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        UserDetails user = userAccountService.register(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, jwtService.createTokenCookie(user.getUsername(), servletRequest.isSecure()).toString())
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserDetails user = (UserDetails) authentication.getPrincipal();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtService.createTokenCookie(user.getUsername(), servletRequest.isSecure()).toString())
                .build();
    }
}