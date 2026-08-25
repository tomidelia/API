package com.uade.tpo.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

/**
 * Levanta la API en un puerto real y le pega por HTTP, igual que Insomnia.
 * Sirve para verificar los status code y que el JSON de respuesta se serialice
 * bien (las relaciones bidireccionales pueden entrar en recursion infinita).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:apitestdb;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1" })
class ApiEndpointsTests {

    @Autowired
    private TestRestTemplate rest;

    @SuppressWarnings("rawtypes")
    @Test
    void elCatalogoRespondeUnJsonPaginado() {
        ResponseEntity<Map> response = rest.getForEntity("/products?page=0&size=10", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKeys("content", "totalElements", "totalPages");
        assertThat(response.getBody().toString()).contains("Catan");
    }

    @SuppressWarnings("rawtypes")
    @Test
    void elDetalleDelProductoTraeImagenesPrecioYDisponibilidad() {
        ResponseEntity<Map> response = rest.getForEntity("/products/1", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKeys("name", "description", "price", "finalPrice", "stock",
                "available", "images", "categoryDescription", "sellerUsername");
    }

    @SuppressWarnings("rawtypes")
    @Test
    void elCatalogoFiltraPorCategoriaYPorTextoDeBusqueda() {
        ResponseEntity<Map> porTexto = rest.getForEntity("/products?search=cartas", Map.class);
        assertThat(porTexto.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Dixit, Uno y Virus! tienen "cartas" en la descripcion
        assertThat(porTexto.getBody().get("totalElements")).isEqualTo(3);

        ResponseEntity<Map> conStock = rest.getForEntity("/products?onlyAvailable=true", Map.class);
        assertThat(conStock.getBody().toString()).doesNotContain("\"name\":\"Uno\"");
    }

    @SuppressWarnings("rawtypes")
    @Test
    void unProductoInvalidoDevuelve400ConElDetalleDelError() {
        ResponseEntity<Map> response = rest.postForEntity("/products", Map.of("name", ""), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message").toString()).contains("obligator");
    }

    @SuppressWarnings("rawtypes")
    @Test
    void pedirUnProductoInexistenteDevuelve404() {
        ResponseEntity<Map> response = rest.getForEntity("/products/9999", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("message").toString()).contains("9999");
    }

    @SuppressWarnings("rawtypes")
    @Test
    void agregarAlCarritoUnProductoSinStockDevuelve400() {
        // "Uno" se carga con stock 0 en los datos de prueba.
        ResponseEntity<Map> productos = rest.getForEntity("/products?search=Uno", Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) productos.getBody().get("content");
        Object unoId = content.get(0).get("id");

        ResponseEntity<Map> response = rest.postForEntity("/carts/3/items",
                Map.of("productId", unoId, "quantity", 1), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message").toString()).contains("stock");
    }

    @SuppressWarnings("rawtypes")
    @Test
    void elCheckoutConCarritoVacioDevuelve400() {
        ResponseEntity<Map> response = rest.postForEntity("/orders/checkout/3", null, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message").toString()).contains("vacio");
    }

    @SuppressWarnings("rawtypes")
    @Test
    void flujoCompletoDeCompraPorHttp() {
        ResponseEntity<Map> productos = rest.getForEntity("/products?search=Virus", Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) productos.getBody().get("content");
        Object virusId = content.get(0).get("id");

        // 15400 con 20% de descuento = 12320 por unidad
        ResponseEntity<Map> carrito = rest.postForEntity("/carts/3/items",
                Map.of("productId", virusId, "quantity", 3), Map.class);
        assertThat(carrito.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(new BigDecimal(carrito.getBody().get("total").toString()))
                .isEqualByComparingTo(new BigDecimal("36960.00"));

        ResponseEntity<Map> orden = rest.postForEntity("/orders/checkout/3", null, Map.class);
        assertThat(orden.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(new BigDecimal(orden.getBody().get("total").toString()))
                .isEqualByComparingTo(new BigDecimal("36960.00"));

        ResponseEntity<Map> productoDespues = rest.getForEntity("/products/" + virusId, Map.class);
        assertThat(productoDespues.getBody().get("stock")).isEqualTo(22);

        ResponseEntity<Map> carritoDespues = rest.getForEntity("/carts/3", Map.class);
        @SuppressWarnings("unchecked")
        List<Object> items = (List<Object>) carritoDespues.getBody().get("items");
        assertThat(items).isEmpty();
    }

    @SuppressWarnings("rawtypes")
    @Test
    void elEndpointDeCategoriasDeLaCatedraSigueRespondiendo() {
        ResponseEntity<Map> response = rest.getForEntity("/categories?page=0&size=10", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().toString()).contains("Estrategia");
    }
}
