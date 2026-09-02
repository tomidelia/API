package com.uade.tpo.demo.controllers.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Para el login solo hacen falta el email y la contrasenia. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {

    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "La contrasenia es obligatoria")
    private String password;
}
