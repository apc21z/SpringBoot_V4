package com.apc21z.proyectv4.service;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.apc21z.proyectv4.jwt.config.AuthenticatedUserDetails;
import com.apc21z.proyectv4.jwt.model.UserAccount;
import com.apc21z.proyectv4.repository.UserAccountRepository;

@Service
public class UserAccountService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDetails register(String email, String rawPassword) {
        String normalizedEmail = normalizeEmail(email);
        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        UserAccount account = new UserAccount(normalizedEmail, passwordEncoder.encode(rawPassword));
        return toUserDetails(userAccountRepository.save(account));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userAccountRepository.findByEmail(normalizeEmail(email))
                .map(this::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private UserDetails toUserDetails(UserAccount account) {
        return new AuthenticatedUserDetails(account);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}