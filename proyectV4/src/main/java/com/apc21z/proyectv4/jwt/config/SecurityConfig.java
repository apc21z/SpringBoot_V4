package com.apc21z.proyectv4.jwt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.apc21z.proyectv4.jwt.service.JwtService;
import com.apc21z.proyectv4.service.UserAccountService;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationProvider authenticationProvider(UserAccountService userAccountService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userAccountService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    @Order(1)
    SecurityFilterChain apiSecurity(HttpSecurity http, JwtService jwtService, UserAccountService userAccountService,
            AuthenticationProvider authenticationProvider) throws Exception {
        http.securityMatcher("/api/**")
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/people", "/api/person").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/people/**", "/api/person/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/people/**", "/api/person/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(
                        (request, response, authException) -> response.sendError(401, "Unauthorized")))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, userAccountService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain webSecurity(HttpSecurity http, JwtService jwtService, UserAccountService userAccountService,
            AuthenticationProvider authenticationProvider) throws Exception {
        http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/register", "/css/**", "/error").permitAll()
                        .requestMatchers("/dashboard").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            response.addHeader(HttpHeaders.SET_COOKIE,
                                    jwtService.createTokenCookie(authentication.getName(), request.isSecure()).toString());
                            response.sendRedirect("/dashboard");
                        })
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.addHeader(HttpHeaders.SET_COOKIE,
                                    jwtService.clearTokenCookie(request.isSecure()).toString());
                            response.sendRedirect("/");
                        })
                        .permitAll())
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, userAccountService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}