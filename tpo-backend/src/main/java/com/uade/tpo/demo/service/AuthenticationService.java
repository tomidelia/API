package com.uade.tpo.demo.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.controllers.auth.AuthenticationRequest;
import com.uade.tpo.demo.controllers.auth.AuthenticationResponse;
import com.uade.tpo.demo.controllers.auth.RegisterRequest;
import com.uade.tpo.demo.controllers.config.JwtService;
import com.uade.tpo.demo.entity.Cart;
import com.uade.tpo.demo.entity.Role;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.exceptions.DuplicateUserException;
import com.uade.tpo.demo.repository.CartRepository;
import com.uade.tpo.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/** Registro y login de la aplicacion. */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Alta de usuario.
     *
     * Dos decisiones de negocio importantes:
     *
     * 1) El rol NO llega en el request: se fuerza a USER. Somos una tienda con
     *    un unico vendedor, asi que todo el que se registra es comprador y no
     *    puede auto-asignarse ADMIN. La cuenta de la tienda se crea aparte.
     *
     * 2) Se le crea el carrito en el mismo momento del registro, para que la
     *    primera vez que agregue un producto ya lo tenga.
     *
     * Va todo en una unica transaccion: si algo falla, no queda ni el usuario
     * a medias ni un carrito huerfano.
     */
    @Transactional(rollbackFor = Throwable.class)
    public AuthenticationResponse register(RegisterRequest request) throws DuplicateUserException {

        if (userRepository.existsByEmail(request.getEmail()))
            throw new DuplicateUserException("Ya existe un usuario con el email " + request.getEmail());

        if (userRepository.existsByNickname(request.getUsername()))
            throw new DuplicateUserException("Ya existe un usuario con el nombre " + request.getUsername());

        User user = new User();
        user.setNickname(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setRole(Role.USER);

        User saved = userRepository.save(user);
        cartRepository.save(new Cart(saved));

        return buildResponse(saved);
    }

    /**
     * Login. El AuthenticationManager es el que compara el email y la
     * contrasenia contra la base; si no coinciden lanza la excepcion el.
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        return buildResponse(user);
    }

    private AuthenticationResponse buildResponse(User user) {
        return AuthenticationResponse.builder()
                .accessToken(jwtService.generateToken(user))
                .role(user.getRole().name())
                .username(user.getNickname())
                .build();
    }
}
