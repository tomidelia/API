package com.uade.tpo.demo.controllers.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Datos necesarios para registrar un nuevo usuario:
 * nombre de usuario, email, contrasenia, nombre y apellido.
 *
 * El rol no se solicita durante el registro. La aplicacion utiliza un unico
 * vendedor, por lo que todos los nuevos usuarios se registran como compradores.
 * El rol correspondiente se asigna desde el servicio.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato valido")
    private String email;

    @NotBlank(message = "La contrasenia es obligatoria")
    @Size(min = 6, message = "La contrasenia debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El apellido es obligatorio")
    private String surname;
}
