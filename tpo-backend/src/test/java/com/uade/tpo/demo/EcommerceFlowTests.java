package com.uade.tpo.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.dto.CartItemRequest;
import com.uade.tpo.demo.entity.dto.CartResponse;
import com.uade.tpo.demo.entity.dto.OrderResponse;
import com.uade.tpo.demo.entity.dto.ProductRequest;
import com.uade.tpo.demo.entity.dto.ProductResponse;
import com.uade.tpo.demo.exceptions.InsufficientStockException;
import com.uade.tpo.demo.repository.ProductRepository;
import com.uade.tpo.demo.repository.UserRepository;
import com.uade.tpo.demo.service.CartService;
import com.uade.tpo.demo.service.OrderService;
import com.uade.tpo.demo.service.ProductService;

/**
 * Recorre los casos de uso principales del enunciado contra una base H2 en
 * memoria: catalogo, filtros, publicacion, stock, descuentos, carrito y
 * checkout.
 */
@SpringBootTest
class EcommerceFlowTests {

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private Long productId(String name) {
        return productRepository.findAll().stream()
                .filter(p -> p.getName().equals(name))
                .map(Product::getId)
                .findFirst()
                .orElseThrow();
    }

    private Long userId(String username) {
        return userRepository.findByUsername(username).orElseThrow().getId();
    }

    @Test
    void elCatalogoDevuelveLosProductosCargados() {
        Page<ProductResponse> page = productService.getProducts(
                null, null, null, false, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(ProductResponse::getName)
                .contains("Catan", "Carcassonne", "Dixit", "Uno", "Virus!");
    }

    @Test
    void sePuedeBuscarPorNombreYFiltrarPorPrecio() {
        Page<ProductResponse> porNombre = productService.getProducts(
                null, null, null, false, "catan", PageRequest.of(0, 10));
        assertThat(porNombre.getContent()).extracting(ProductResponse::getName).containsExactly("Catan");

        Page<ProductResponse> baratos = productService.getProducts(
                null, null, new BigDecimal("20000"), false, null, PageRequest.of(0, 10));
        assertThat(baratos.getContent()).extracting(ProductResponse::getName)
                .containsExactlyInAnyOrder("Uno", "Virus!");
    }

    @Test
    void elFiltroDeDisponiblesDejaAfueraLosProductosSinStock() {
        Page<ProductResponse> disponibles = productService.getProducts(
                null, null, null, true, null, PageRequest.of(0, 10));

        assertThat(disponibles.getContent()).extracting(ProductResponse::getName).doesNotContain("Uno");
    }

    @Test
    void elDescuentoSeReflejaEnElPrecioFinal() {
        ProductResponse carcassonne = productService.getProducts(
                null, null, null, false, "carcassonne", PageRequest.of(0, 1)).getContent().get(0);

        // 42500 con 15% de descuento
        assertThat(carcassonne.getFinalPrice()).isEqualByComparingTo(new BigDecimal("36125.00"));
    }

    @Test
    void sePublicaUnProductoConVariasFotos() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Aventureros al Tren");
        request.setDescription("Juego de rutas ferroviarias para 2 a 5 jugadores.");
        request.setPrice(new BigDecimal("70000.00"));
        request.setStock(3);
        request.setDiscount(10);
        request.setCategoryId(productRepository.findAll().get(0).getCategory().getId());
        request.setImages(List.of("https://placehold.co/600x600?text=Ticket1",
                "https://placehold.co/600x600?text=Ticket2"));

        ProductResponse creado = productService.createProduct(request);

        assertThat(creado.getId()).isNotNull();
        assertThat(creado.getImages()).hasSize(2);
        assertThat(creado.getFinalPrice()).isEqualByComparingTo(new BigDecimal("63000.00"));
        assertThat(creado.isAvailable()).isTrue();
    }

    @Test
    void noSePuedeAgregarAlCarritoUnProductoSinStock() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(productId("Uno"));
        request.setQuantity(1);

        Assertions.assertThrows(InsufficientStockException.class,
                () -> cartService.addItem(userId("sofia"), request));
    }

    @Test
    void noSePuedeAgregarMasCantidadQueElStockDisponible() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(productId("Dixit"));
        request.setQuantity(99);

        Assertions.assertThrows(InsufficientStockException.class,
                () -> cartService.addItem(userId("sofia"), request));
    }

    @Test
    void elCheckoutCalculaElTotalYDescuentaElStock() throws Exception {
        Long comprador = userId("martin");
        Long catan = productId("Catan");
        int stockInicial = productRepository.findById(catan).orElseThrow().getStock();

        CartItemRequest request = new CartItemRequest();
        request.setProductId(catan);
        request.setQuantity(2);

        CartResponse carrito = cartService.addItem(comprador, request);
        assertThat(carrito.getItems()).hasSize(1);
        assertThat(carrito.getTotal()).isEqualByComparingTo(new BigDecimal("109998.00"));

        OrderResponse orden = orderService.checkout(comprador);

        assertThat(orden.getId()).isNotNull();
        assertThat(orden.getTotal()).isEqualByComparingTo(new BigDecimal("109998.00"));
        assertThat(orden.getItems()).hasSize(1);
        assertThat(orden.getItems().get(0).getQuantity()).isEqualTo(2);

        assertThat(productRepository.findById(catan).orElseThrow().getStock()).isEqualTo(stockInicial - 2);
        assertThat(cartService.getCart(comprador).getItems()).isEmpty();
    }
}
