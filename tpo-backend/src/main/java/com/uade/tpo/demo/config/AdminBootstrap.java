package com.uade.tpo.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Role;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.repository.UserRepository;

/**
 * Crea la cuenta de la tienda la primera vez que arranca, si todavía no existe
 * ninguna.
 *
 * Esto no es "cargar datos hardcodeados": es el arranque en frío del sistema.
 * Como el registro público siempre da rol USER (somos una tienda con un único
 * vendedor), sin esta cuenta no habría forma de que exista un ADMIN y nadie
 * podría cargar el primer producto.
 *
 * El email y la contraseña salen del application.properties, no del código.
 */
@Component
@Order(1)
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmail(adminEmail))
            return;

        User admin = new User();
        admin.setNickname("admin");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setName("Tienda");
        admin.setSurname("Juegos de Mesa");
        admin.setRole(Role.ADMIN);

        userRepository.save(admin);

        log.info("Cuenta de la tienda creada: {} (rol ADMIN)", adminEmail);
    }
}
