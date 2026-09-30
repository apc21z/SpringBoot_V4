package com.apc21z.proyectv4.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apc21z.proyectv4.dto.AuthResponse;
import com.apc21z.proyectv4.dto.LoginRequest;
import com.apc21z.proyectv4.dto.RegisterRequest;
import com.apc21z.proyectv4.service.JwtService;
import com.apc21z.proyectv4.service.UserAccountService;

import jakarta.validation.Valid;

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
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserDetails user = userAccountService.register(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(createResponse(user));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        return createResponse((UserDetails) authentication.getPrincipal());
    }

    private AuthResponse createResponse(UserDetails user) {
        return new AuthResponse(jwtService.generateToken(user), "Bearer", jwtService.getExpirationSeconds());
    }
}