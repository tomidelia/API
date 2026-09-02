package com.uade.tpo.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.RoleRequest;
import com.uade.tpo.demo.entity.dto.UserResponse;
import com.uade.tpo.demo.exceptions.ForbiddenActionException;
import com.uade.tpo.demo.exceptions.UserNotFoundException;
import com.uade.tpo.demo.service.UserService;

import jakarta.validation.Valid;

/**
 * Administracion de cuentas de usuario.
 * Todo este controller es exclusivo del ADMIN (ver SecurityConfig).
 *
 * El registro y el login se gestionan en AuthenticationController,
 * bajo /api/v1/auth.
 */
@RestController
@RequestMapping("users")
public class UsersController {

    @Autowired
    private UserService userService;

    /** Listado de cuentas, paginado. Nunca devuelve contrasenias. */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        PageRequest pageRequest = (page == null || size == null)
                ? PageRequest.of(0, Integer.MAX_VALUE)
                : PageRequest.of(page, size);

        return ResponseEntity.ok(userService.getUsers(pageRequest));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) throws UserNotFoundException {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    /** Asignacion de permisos: cambia el rol de una cuenta. */
    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserResponse> updateRole(
            @AuthenticationPrincipal User admin,
            @PathVariable Long userId,
            @Valid @RequestBody RoleRequest roleRequest)
            throws UserNotFoundException, ForbiddenActionException {

        return ResponseEntity.ok(userService.updateRole(userId, admin.getId(), roleRequest));
    }
}
