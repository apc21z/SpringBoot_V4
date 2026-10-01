package com.apc21z.proyectv4;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.apc21z.proyectv4.jwt.config.JwtProperties;
import com.apc21z.proyectv4.jwt.model.UserAccount;
import com.apc21z.proyectv4.repository.UserAccountRepository;

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
				admin.setRole("ADMIN");
				userAccountRepository.save(admin);
			}
		};
	}

}