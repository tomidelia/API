package com.uade.tpo.demo.entity.dto;

import com.uade.tpo.demo.entity.Role;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Asignacion de permisos: el rol nuevo que se le da a una cuenta. */
@Data
public class RoleRequest {

    @NotNull(message = "El rol es obligatorio (USER o ADMIN)")
    private Role role;
}
