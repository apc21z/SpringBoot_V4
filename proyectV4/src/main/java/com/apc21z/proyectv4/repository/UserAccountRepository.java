package com.apc21z.proyectv4.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apc21z.proyectv4.model.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);
}