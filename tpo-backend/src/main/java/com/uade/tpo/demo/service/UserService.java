package com.uade.tpo.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.uade.tpo.demo.entity.dto.RoleRequest;
import com.uade.tpo.demo.entity.dto.UserResponse;
import com.uade.tpo.demo.exceptions.ForbiddenActionException;
import com.uade.tpo.demo.exceptions.UserNotFoundException;

/**
 * Administracion de cuentas de usuario.
 *
 * El registro, el login y el hasheo de contrasenias se gestionan
 * en AuthenticationService.
 */
public interface UserService {

    public Page<UserResponse> getUsers(PageRequest pageRequest);

    public UserResponse getUserById(Long userId) throws UserNotFoundException;

    /** Asignacion de permisos: cambia el rol de una cuenta. */
    public UserResponse updateRole(Long userId, Long adminId, RoleRequest roleRequest)
            throws UserNotFoundException, ForbiddenActionException;
}
