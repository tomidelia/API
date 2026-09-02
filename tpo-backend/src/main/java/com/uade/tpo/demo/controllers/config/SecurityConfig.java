package com.uade.tpo.demo.controllers.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.uade.tpo.demo.entity.Role;

import lombok.RequiredArgsConstructor;

/**
 * Define quien puede entrar a cada endpoint.
 *
 * El recorrido esta pensado como el de cualquier e-commerce: se puede mirar el
 * catalogo sin estar registrado, pero para comprar hay que loguearse, y para
 * administrar los productos hay que ser la tienda.
 *
 * IMPORTANTE: el ultimo anyRequest().authenticated() es la red de seguridad.
 * Cualquier endpoint que nos olvidemos de listar aca arriba cae ahi y pide
 * token, en vez de quedar abierto por accidente.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(req -> req

                        // ---------------- PUBLICO (sin token) ----------------
                        // Registro y login: no se puede pedir token a quien
                        // todavia no lo tiene.
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/error/**").permitAll()

                        // El catalogo se navega sin estar registrado, igual que
                        // Catalogo publico: home, filtros y detalle del producto.
                        .requestMatchers(HttpMethod.GET, "/products").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()

                        // ---------------- SOLO LA TIENDA (ADMIN) ----------------
                        // Alta, modificacion, stock, descuentos y baja de productos.
                        .requestMatchers(HttpMethod.POST, "/products").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/products/**").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PATCH, "/products/**").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/products/**").hasAuthority(Role.ADMIN.name())

                        // Solo la tienda define el arbol de categorias.
                        .requestMatchers(HttpMethod.POST, "/categories").hasAuthority(Role.ADMIN.name())

                        // Administracion de cuentas y asignacion de permisos.
                        .requestMatchers("/users/**").hasAuthority(Role.ADMIN.name())

                        // ---------------- SOLO COMPRADORES (USER) ----------------
                        // El carrito y las compras son del comprador. La tienda
                        // no compra, asi que el ADMIN queda afuera a proposito.
                        .requestMatchers("/carts/**").hasAuthority(Role.USER.name())
                        .requestMatchers("/orders/**").hasAuthority(Role.USER.name())

                        // ---------------- RED DE SEGURIDAD ----------------
                        .anyRequest().authenticated())

                // Sin sesiones: el estado del usuario viaja en el token, no
                // queda guardado en la memoria del servidor.
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)

                // El filtro de JWT corre ANTES que todo lo demas: si el token no
                // pasa, la request no llega nunca al controller.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
