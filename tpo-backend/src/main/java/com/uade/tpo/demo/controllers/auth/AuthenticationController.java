package com.uade.tpo.demo.controllers.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.exceptions.DuplicateUserException;
import com.uade.tpo.demo.service.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Los dos únicos endpoints públicos de la aplicación: sin ellos nadie podría
 * conseguir un token.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    /** Alta de un comprador. Devuelve el token ya listo para usar. */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request)
            throws DuplicateUserException {
        return ResponseEntity.ok(service.register(request));
    }

    /** Login por email y contraseña. */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}
