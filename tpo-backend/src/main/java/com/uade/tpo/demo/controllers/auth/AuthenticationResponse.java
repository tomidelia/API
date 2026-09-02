package com.uade.tpo.demo.controllers.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lo que devuelven el registro y el login. En lugar del objeto usuario se
 * devuelve el token: es lo unico que el cliente necesita para las proximas
 * requests.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    @JsonProperty("access_token")
    private String accessToken;

    /** Se devuelve para que el frontend sepa que puede mostrar sin probar. */
    private String role;

    private String username;
}
