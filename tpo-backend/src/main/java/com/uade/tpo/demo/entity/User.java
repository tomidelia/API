package com.uade.tpo.demo.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Usuario de la aplicacion.
 *
 * Implementa UserDetails para integrarse con Spring Security.
 * Spring identifica al usuario mediante el valor devuelto por
 * getUsername(): en este caso el email utilizado para iniciar sesion.
 */
@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = { "cart", "orders", "password" })
@Entity
@Table(name = "users")
public class User implements UserDetails {

    public User() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
* Nombre de usuario elegido durante el registro.
 * Se guarda en la columna "username", mientras que la autenticacion
 * se realiza mediante email.
 */
    @Column(name = "username", nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    /** Se guarda hasheada con BCrypt, nunca en texto plano. */
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Relacion OneToOne: cada usuario tiene un unico carrito.
    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Cart cart;

    // Relacion OneToMany: un usuario genera muchas ordenes.
    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<Order> orders = new ArrayList<>();

    // ---------------------------------------------------------------------
    // Metodos que exige UserDetails
    // ---------------------------------------------------------------------

    /**
     * Los permisos del usuario. Son lo que SecurityConfig compara cuando
     * escribimos hasAuthority(Role.ADMIN.name()).
     */
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /** Para Spring Security el "username" es el email: con eso se loguea. */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}
