package com.uade.tpo.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uade.tpo.demo.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Con el email se hace el login: es el username para Spring Security. */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}
