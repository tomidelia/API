package com.uade.tpo.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

/**
 * Levanta la API en un puerto real y le pega por HTTP, igual que Insomnia.
 *
 * Verifica las tres capas de la seguridad: que el catalogo siga siendo publico,
 * que lo protegido rechace a quien no trae token, y que un comprador no pueda
 * hacer cosas de la tienda aunque este logueado.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:apitestdb;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1" })
class ApiEndpointsTests {

    @Autowired
    private TestRestTemplate rest;

    // ---------------------------------------------------------------- helpers

    @SuppressWarnings("rawtypes")
    private String tokenDe(String email, String password) {
        ResponseEntity<Map> response = rest.postForEntity("/api/v1/auth/authenticate",
                Map.of("email", email, "password", password), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody().get("access_token").toString();
    }

    private String tokenAdmin() {
        return tokenDe("admin@juegosdemesa.com", "123456");
    }

    private String tokenComprador() {
        return tokenDe("sofia@mail.com", "123456");
    }

    @SuppressWarnings("rawtypes")
    private ResponseEntity<Map> conToken(String token, HttpMethod method, String url, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null)
            headers.setBearerAuth(token);
        return rest.exchange(url, method, new HttpEntity<>(body, headers), Map.class);
    }

    // ------------------------------------------------- registro y login

    @SuppressWarnings("rawtypes")
    @Test
    void elRegistroDevuelveUnTokenYSiempreCreaCompradores() {
        ResponseEntity<Map> response = rest.postForEntity("/api/v1/auth/register", Map.of(
                "username", "nuevoUsuario",
                "email", "nuevo@mail.com",
                "password", "123456",
                "name", "Juan",
                "surname", "Perez"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("access_token").toString()).contains(".");
        // Aunque el request no manda rol, el servicio lo fuerza a USER.
        assertThat(response.getBody().get("role")).isEqualTo("USER");
    }

    @SuppressWarnings("rawtypes")
    @Test
    void noSePuedeRegistrarDosVecesElMismoEmail() {
        Map<String, String> body = Map.of(
                "username", "repetido", "email", "repetido@mail.com",
                "password", "123456", "name", "Ana", "surname", "Lopez");

        assertThat(rest.postForEntity("/api/v1/auth/register", body, Map.class)
                .getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> segunda = rest.postForEntity("/api/v1/auth/register", body, Map.class);
        assertThat(segunda.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(segunda.getBody().get("message").toString()).contains("Ya existe");
    }

    @SuppressWarnings("rawtypes")
    @Test
    void elRegistroValidaLosDatosObligatorios() {
        ResponseEntity<Map> response = rest.postForEntity("/api/v1/auth/register",
                Map.of("username", "", "email", "no-es-un-mail", "password", "123"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @SuppressWarnings("rawtypes")
    @Test
    void elLoginConDatosMalDevuelve401ConMensajeGenerico() {
        ResponseEntity<Map> response = rest.postForEntity("/api/v1/auth/authenticate",
                Map.of("email", "sofia@mail.com", "password", "equivocada"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        // No aclara si fallo el email o la contrasenia, a proposito.
        assertThat(response.getBody().get("message").toString()).isEqualTo("Email o contrasenia incorrectos");
    }

    // ------------------------------------------------- endpoints publicos

    @SuppressWarnings("rawtypes")
    @Test
    void elCatalogoSeNavegaSinEstarLogueado() {
        ResponseEntity<Map> lista = rest.getForEntity("/products?page=0&size=10", Map.class);
        assertThat(lista.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(lista.getBody().toString()).contains("Catan");

        assertThat(rest.getForEntity("/products/1", Map.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rest.getForEntity("/products?search=cartas", Map.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rest.getForEntity("/categories", Map.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ------------------------------------------------- sin token

    @SuppressWarnings("rawtypes")
    @Test
    void sinTokenNoSePuedeUsarElCarritoNiComprar() {
        assertThat(rest.getForEntity("/carts", Map.class).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(rest.postForEntity("/orders/checkout", null, Map.class)
                .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(rest.getForEntity("/orders/my", Map.class).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @SuppressWarnings("rawtypes")
    @Test
    void sinTokenNoSePuedePublicarNiBorrarProductos() {
        assertThat(rest.postForEntity("/products", Map.of("name", "x"), Map.class)
                .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(conToken(null, HttpMethod.DELETE, "/products/1", null)
                .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @SuppressWarnings("rawtypes")
    @Test
    void unTokenManipuladoNoSirve() {
        String token = tokenComprador();
        String manipulado = token.substring(0, token.length() - 4) + "AAAA";

        assertThat(conToken(manipulado, HttpMethod.GET, "/carts", null)
                .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ------------------------------------------------- separacion de roles

    @SuppressWarnings("rawtypes")
    @Test
    void unCompradorNoPuedePublicarProductos() {
        ResponseEntity<Map> response = conToken(tokenComprador(), HttpMethod.POST, "/products", Map.of(
                "name", "Juego pirata", "description", "No deberia poder crearse.",
                "price", 1000, "stock", 1, "categoryId", 1,
                "images", List.of("https://placehold.co/600x600?text=X")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @SuppressWarnings("rawtypes")
    @Test
    void unCompradorNoPuedeCrearCategorias() {
        assertThat(conToken(tokenComprador(), HttpMethod.POST, "/categories", Map.of("description", "Pirata"))
                .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @SuppressWarnings("rawtypes")
    @Test
    void laTiendaNoUsaElCarrito() {
        // El ADMIN vende, no compra: el carrito es solo para el rol USER.
        assertThat(conToken(tokenAdmin(), HttpMethod.GET, "/carts", null)
                .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @SuppressWarnings("rawtypes")
    @Test
    void laTiendaSiPuedePublicarYAdministrarProductos() {
        String admin = tokenAdmin();

        ResponseEntity<Map> creado = conToken(admin, HttpMethod.POST, "/products", Map.of(
                "name", "Aventureros al Tren", "description", "Juego de rutas ferroviarias.",
                "price", 70000.00, "stock", 5, "discount", 10, "categoryId", 1,
                "images", List.of("https://placehold.co/600x600?text=Ticket")));

        assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Object id = creado.getBody().get("id");

        assertThat(conToken(admin, HttpMethod.PATCH, "/products/" + id + "/stock", Map.of("stock", 20))
                .getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(conToken(admin, HttpMethod.PATCH, "/products/" + id + "/discount", Map.of("discount", 25))
                .getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(conToken(admin, HttpMethod.DELETE, "/products/" + id, null)
                .getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    // ------------------------------------------------- flujo de compra

    @SuppressWarnings("rawtypes")
    @Test
    void flujoCompletoDeCompraConToken() {
        String comprador = tokenDe("martin@mail.com", "123456");

        ResponseEntity<Map> productos = rest.getForEntity("/products?search=Virus", Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) productos.getBody().get("content");
        Object virusId = content.get(0).get("id");

        // 15400 con 20% de descuento = 12320 por unidad
        ResponseEntity<Map> carrito = conToken(comprador, HttpMethod.POST, "/carts/items",
                Map.of("productId", virusId, "quantity", 3));
        assertThat(carrito.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(new BigDecimal(carrito.getBody().get("total").toString()))
                .isEqualByComparingTo(new BigDecimal("36960.00"));

        ResponseEntity<Map> orden = conToken(comprador, HttpMethod.POST, "/orders/checkout", null);
        assertThat(orden.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(new BigDecimal(orden.getBody().get("total").toString()))
                .isEqualByComparingTo(new BigDecimal("36960.00"));

        assertThat(rest.getForEntity("/products/" + virusId, Map.class).getBody().get("stock")).isEqualTo(22);

        @SuppressWarnings("unchecked")
        List<Object> items = (List<Object>) conToken(comprador, HttpMethod.GET, "/carts", null)
                .getBody().get("items");
        assertThat(items).isEmpty();
    }

    @SuppressWarnings("rawtypes")
    @Test
    void unCompradorNoVeLasOrdenesDeOtro() {
        // martin genera una orden en el test de arriba; sofia no debe poder verla.
        String martin = tokenDe("martin@mail.com", "123456");
        conToken(martin, HttpMethod.POST, "/carts/items", Map.of("productId", 2, "quantity", 1));
        ResponseEntity<Map> orden = conToken(martin, HttpMethod.POST, "/orders/checkout", null);
        Object ordenId = orden.getBody().get("id");

        ResponseEntity<Map> ajena = conToken(tokenComprador(), HttpMethod.GET, "/orders/" + ordenId, null);

        // Responde 404 y no 403: no le confirma a nadie que esa orden existe.
        assertThat(ajena.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @SuppressWarnings("rawtypes")
    @Test
    void elCarritoValidaElStockAunEstandoLogueado() {
        // "Uno" se carga con stock 0 en los datos de prueba.
        ResponseEntity<Map> productos = rest.getForEntity("/products?search=Uno", Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) productos.getBody().get("content");
        Object unoId = content.get(0).get("id");

        ResponseEntity<Map> response = conToken(tokenComprador(), HttpMethod.POST, "/carts/items",
                Map.of("productId", unoId, "quantity", 1));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message").toString()).contains("stock");
    }
}
