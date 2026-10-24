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
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.uade.tpo.demo.entity.Role;

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

/**
 * Define quién puede entrar a cada endpoint.
 *
 * El recorrido está pensado como el de cualquier e-commerce: se puede mirar el
 * catálogo sin estar registrado, pero para comprar hay que loguearse, y para
 * administrar los productos hay que ser la tienda.
 *
 * IMPORTANTE: el último anyRequest().authenticated() es la red de seguridad.
 * Cualquier endpoint que nos olvidemos de listar acá arriba cae ahí y pide
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

                        // ---------------- PÚBLICO (sin token) ----------------
                        // Registro y login: no se puede pedir token a quien
                        // todavía no lo tiene.
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/error/**").permitAll()

                        // El catálogo se navega sin estar registrado, igual que
                        // Catálogo público: home, filtros y detalle del producto.
                        .requestMatchers(HttpMethod.GET, "/products").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()

                        // ---------------- SOLO LA TIENDA (ADMIN) ----------------
                        // Alta, modificación, stock, descuentos y baja de productos.
                        .requestMatchers(HttpMethod.POST, "/products").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/products/**").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PATCH, "/products/**").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/products/**").hasAuthority(Role.ADMIN.name())

                        // Solo la tienda define el árbol de categorías.
                        .requestMatchers(HttpMethod.POST, "/categories").hasAuthority(Role.ADMIN.name())

                        // Administración de cuentas y asignación de permisos.
                        .requestMatchers("/users/**").hasAuthority(Role.ADMIN.name())

                        // ---------------- SOLO COMPRADORES (USER) ----------------
                        // El carrito y las compras son del comprador. La tienda
                        // no compra, así que el ADMIN queda afuera a propósito.
                        .requestMatchers("/carts/**").hasAuthority(Role.USER.name())
                        .requestMatchers("/orders/**").hasAuthority(Role.USER.name())

                        // ---------------- RED DE SEGURIDAD ----------------
                        .anyRequest().authenticated())

                // Sin sesiones: el estado del usuario viaja en el token, no
                // queda guardado en la memoria del servidor.
                // Sin esto Spring responde 403 a todo: tanto a quien no mandó
                // token como a quien mandó uno válido pero sin el rol que hace
                // falta. Son dos situaciones distintas y el cliente necesita
                // poder distinguirlas: con 401 sabe que tiene que volver a
                // loguearse, y con 403 que esa cuenta no alcanza.
                .exceptionHandling(manejo -> manejo.authenticationEntryPoint(sinAutenticar()))
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)

                // El filtro de JWT corre ANTES que todo lo demás: si el token no
                // pasa, la request no llega nunca al controller.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Qué responder cuando la request no trae una autenticación válida: no
     * mandó token, lo mandó vencido, o lo mandó adulterado.
     *
     * Devuelve 401 con el mismo cuerpo de error que usa el resto de la API,
     * para que el cliente lea siempre el mismo formato.
     */
    @Bean
    public AuthenticationEntryPoint sinAutenticar() {
        return (request, response, excepcion) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("""
                    {"status":401,"error":"Unauthorized",                    "message":"Tenés que iniciar sesión para hacer esto."}""");
        };
    }
}
