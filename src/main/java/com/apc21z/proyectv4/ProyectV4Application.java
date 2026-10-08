package com.apc21z.proyectv4;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.apc21z.proyectv4.rest.repository.UserAccountRepository;
import com.apc21z.proyectv4.security.jwt.config.JwtProperties;
import com.apc21z.proyectv4.security.jwt.model.Role;
import com.apc21z.proyectv4.security.jwt.model.UserAccount;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class ProyectV4Application {

	public static void main(String[] args) {
		SpringApplication.run(ProyectV4Application.class, args);
	}

	@Bean
	CommandLineRunner seedAdmin(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			String email = "admin@admin.com";
			if (userAccountRepository.findByEmail(email).isEmpty()) {
				UserAccount admin = new UserAccount(email, passwordEncoder.encode("admin123"));
				admin.setRoles(List.of(Role.ADMIN));
				userAccountRepository.save(admin);
			}
		};
	}

}