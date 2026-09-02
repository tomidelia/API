package com.uade.tpo.demo.entity.dto;

import com.uade.tpo.demo.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Vista de un usuario para la administracion. Nunca expone la contrasenia. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String name;
    private String surname;
    private String role;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getNickname())
                .email(user.getEmail())
                .name(user.getName())
                .surname(user.getSurname())
                .role(user.getRole().name())
                .build();
    }
}
