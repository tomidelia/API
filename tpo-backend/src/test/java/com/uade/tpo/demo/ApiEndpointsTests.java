package com.uade.tpo.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
        return tokenDe("admin@juegosdemesa.com", "TestAdminPassword123!");
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

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("name", "Producto sin autorizacion");
    body.add("description", "No deberia poder crearse.");
    body.add("price", "1000");
    body.add("stock", "1");
    body.add("categoryId", "1");

    ByteArrayResource image = new ByteArrayResource(new byte[] { 1, 2, 3 }) {
        @Override
        public String getFilename() {
            return "producto.jpg";
        }
    };

    body.add("images", image);

    ResponseEntity<Map> response = rest.exchange(
            "/products",
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

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

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    headers.setBearerAuth(tokenComprador());

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("name", "Juego pirata");
    body.add("description", "No deberia poder crearse.");
    body.add("price", "1000");
    body.add("stock", "1");
    body.add("categoryId", "1");

    ByteArrayResource image = new ByteArrayResource(new byte[] { 1, 2, 3 }) {
        @Override
        public String getFilename() {
            return "pirata.jpg";
        }
    };

    body.add("images", image);

    ResponseEntity<Map> response = rest.exchange(
            "/products",
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            Map.class);

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

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    headers.setBearerAuth(admin);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("name", "Aventureros al Tren");
    body.add("description", "Juego de rutas ferroviarias.");
    body.add("price", "70000.00");
    body.add("stock", "5");
    body.add("discount", "10");
    body.add("categoryId", "1");

    ByteArrayResource image = new ByteArrayResource(new byte[] { 1, 2, 3, 4 }) {
        @Override
        public String getFilename() {
            return "ticket.jpg";
        }
    };

    body.add("images", image);

    ResponseEntity<Map> creado = rest.exchange(
            "/products",
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            Map.class);

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

    // ------------------------------------------------- administracion de cuentas

    @SuppressWarnings("rawtypes")
    @Test
    void laTiendaPuedeListarLasCuentas() {
        ResponseEntity<Map> response = conToken(tokenAdmin(), HttpMethod.GET, "/users", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Nunca expone contrasenias, ni siquiera hasheadas.
        assertThat(response.getBody().toString()).doesNotContain("password");
    }

    @SuppressWarnings("rawtypes")
    @Test
    void unCompradorNoPuedeVerLasCuentas() {
        assertThat(conToken(tokenComprador(), HttpMethod.GET, "/users", null)
                .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @SuppressWarnings("rawtypes")
    @Test
    void laTiendaPuedeAsignarPermisos() {
        String admin = tokenAdmin();

        ResponseEntity<Map> alta = rest.postForEntity("/api/v1/auth/register", Map.of(
                "username", "ascendido", "email", "ascendido@mail.com", "password", "123456",
                "name", "Lucia", "surname", "Diaz"), Map.class);
        assertThat(alta.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> usuarios = conToken(admin, HttpMethod.GET, "/users", null);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) usuarios.getBody().get("content");
        Object id = content.stream()
                .filter(u -> "ascendido@mail.com".equals(u.get("email")))
                .findFirst().orElseThrow().get("id");

        ResponseEntity<Map> cambio = conToken(admin, HttpMethod.PATCH, "/users/" + id + "/role",
                Map.of("role", "ADMIN"));

        assertThat(cambio.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cambio.getBody().get("role")).isEqualTo("ADMIN");
    }

    @SuppressWarnings("rawtypes")
    @Test
    void unAdminNoPuedeCambiarseElRolASiMismo() {
        String admin = tokenAdmin();

        ResponseEntity<Map> usuarios = conToken(admin, HttpMethod.GET, "/users", null);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) usuarios.getBody().get("content");
        Object id = content.stream()
                .filter(u -> "admin@juegosdemesa.com".equals(u.get("email")))
                .findFirst().orElseThrow().get("id");

        ResponseEntity<Map> cambio = conToken(admin, HttpMethod.PATCH, "/users/" + id + "/role",
                Map.of("role", "USER"));

        // Si pudiera, la tienda podria quedarse sin ningun administrador.
        assertThat(cambio.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @SuppressWarnings("rawtypes")
@Test
void noSePuedeCrearProductoConImagenVacia() {

    String admin = tokenAdmin();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    headers.setBearerAuth(admin);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("name", "Producto sin imagen");
    body.add("description", "Debe fallar porque la imagen esta vacia.");
    body.add("price", "1000");
    body.add("stock", "1");
    body.add("categoryId", "1");

    ByteArrayResource image = new ByteArrayResource(new byte[] {}) {
        @Override
        public String getFilename() {
            return "vacia.jpg";
        }
    };

    body.add("images", image);

    ResponseEntity<Map> response = rest.exchange(
            "/products",
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
}
}

