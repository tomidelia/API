package com.uade.tpo.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

import com.uade.tpo.demo.controllers.auth.RegisterRequest;
import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.dto.CartItemRequest;
import com.uade.tpo.demo.entity.dto.CartItemUpdateRequest;
import com.uade.tpo.demo.entity.dto.CartResponse;
import com.uade.tpo.demo.entity.dto.OrderResponse;
import com.uade.tpo.demo.entity.dto.ProductRequest;
import com.uade.tpo.demo.exceptions.CartItemNotFoundException;
import com.uade.tpo.demo.exceptions.EmptyCartException;
import com.uade.tpo.demo.exceptions.InsufficientStockException;
import com.uade.tpo.demo.exceptions.OrderNotFoundException;
import com.uade.tpo.demo.repository.CategoryRepository;
import com.uade.tpo.demo.repository.ProductRepository;
import com.uade.tpo.demo.repository.UserRepository;
import com.uade.tpo.demo.service.AuthenticationService;
import com.uade.tpo.demo.service.CartService;
import com.uade.tpo.demo.service.OrderService;
import com.uade.tpo.demo.service.ProductService;

/**
 * Tests del carrito, el checkout y las ordenes.
 *
 * Cada test se arma sus propios datos (usuario y productos con nombre unico),
 * asi ninguno depende del orden en que corran ni de lo que hayan dejado los
 * demas.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:carritotestdb;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1" })
class CarritoCheckoutOrdenesTests {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private static final AtomicInteger SEQ = new AtomicInteger();

    // ------------------------------------------------------------- fixtures

    /** Registra un comprador nuevo y devuelve su id. */
    private Long nuevoComprador() throws Exception {
        int i = SEQ.incrementAndGet();
        RegisterRequest request = RegisterRequest.builder()
                .username("comprador" + i)
                .email("comprador" + i + "@test.com")
                .password("123456")
                .name("Comprador")
                .surname("Test" + i)
                .build();

        authenticationService.register(request);
        return userRepository.findByEmail(request.getEmail()).orElseThrow().getId();
    }

    /** Publica un producto con el precio, stock y descuento que pida el test. */
    private Long nuevoProducto(BigDecimal precio, int stock, int descuento) throws Exception {
        int i = SEQ.incrementAndGet();
        Category categoria = categoryRepository.findAll().stream().findFirst()
                .orElseGet(() -> categoryRepository.save(new Category("Test")));

        ProductRequest request = new ProductRequest();
        request.setName("Juego de prueba " + i);
        request.setDescription("Producto creado por los tests.");
        request.setPrice(precio);
        request.setStock(stock);
        request.setDiscount(descuento);
        request.setCategoryId(categoria.getId());
        request.setImages(List.of(new MockMultipartFile(
                "images", "foto.jpg", "image/jpeg", new byte[] { 1, 2, 3 })));

        return productService.createProduct(request).getId();
    }

    private CartItemRequest item(Long productId, int cantidad) {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(productId);
        request.setQuantity(cantidad);
        return request;
    }

    private int stockDe(Long productId) {
        return productRepository.findById(productId).orElseThrow().getStock();
    }

    // =========================================================== CARRITO

    @Test
    void elCarritoArrancaVacio() throws Exception {
        CartResponse carrito = cartService.getCart(nuevoComprador());

        assertThat(carrito.getItems()).isEmpty();
        assertThat(carrito.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void agregarUnProductoCalculaElSubtotalConElDescuento() throws Exception {
        Long comprador = nuevoComprador();
        // 10000 con 20% de descuento = 8000 por unidad
        Long producto = nuevoProducto(new BigDecimal("10000.00"), 50, 20);

        CartResponse carrito = cartService.addItem(comprador, item(producto, 3));

        assertThat(carrito.getItems()).hasSize(1);
        assertThat(carrito.getItems().get(0).getUnitPrice()).isEqualByComparingTo(new BigDecimal("8000.00"));
        assertThat(carrito.getItems().get(0).getSubtotal()).isEqualByComparingTo(new BigDecimal("24000.00"));
        assertThat(carrito.getTotal()).isEqualByComparingTo(new BigDecimal("24000.00"));
    }

    @Test
    void agregarDosVecesElMismoProductoAcumulaEnUnaSolaLinea() throws Exception {
        Long comprador = nuevoComprador();
        Long producto = nuevoProducto(new BigDecimal("1000.00"), 50, 0);

        cartService.addItem(comprador, item(producto, 2));
        CartResponse carrito = cartService.addItem(comprador, item(producto, 3));

        // No se crea una linea nueva: se suma a la que ya existia.
        assertThat(carrito.getItems()).hasSize(1);
        assertThat(carrito.getItems().get(0).getQuantity()).isEqualTo(5);
        assertThat(carrito.getTotal()).isEqualByComparingTo(new BigDecimal("5000.00"));
    }

    @Test
    void elTotalEsLaSumaDeLosSubtotales() throws Exception {
        Long comprador = nuevoComprador();
        Long a = nuevoProducto(new BigDecimal("1000.00"), 50, 0);   // 2 x 1000 = 2000
        Long b = nuevoProducto(new BigDecimal("2000.00"), 50, 50);  // 1 x 1000 = 1000

        cartService.addItem(comprador, item(a, 2));
        CartResponse carrito = cartService.addItem(comprador, item(b, 1));

        assertThat(carrito.getItems()).hasSize(2);
        assertThat(carrito.getTotal()).isEqualByComparingTo(new BigDecimal("3000.00"));
    }

    @Test
    void noSePuedeAgregarUnProductoSinStock() throws Exception {
        Long comprador = nuevoComprador();
        Long agotado = nuevoProducto(new BigDecimal("1000.00"), 0, 0);

        InsufficientStockException error = Assertions.assertThrows(InsufficientStockException.class,
                () -> cartService.addItem(comprador, item(agotado, 1)));

        assertThat(error.getMessage()).contains("no tiene stock disponible");
    }

    @Test
    void noSePuedeAgregarMasCantidadQueElStock() throws Exception {
        Long comprador = nuevoComprador();
        Long producto = nuevoProducto(new BigDecimal("1000.00"), 3, 0);

        InsufficientStockException error = Assertions.assertThrows(InsufficientStockException.class,
                () -> cartService.addItem(comprador, item(producto, 4)));

        // El mensaje le dice al usuario cuanto hay disponible.
        assertThat(error.getMessage()).contains("Disponible: 3").contains("solicitado: 4");
    }

    @Test
    void laAcumulacionTambienRespetaElStock() throws Exception {
        Long comprador = nuevoComprador();
        Long producto = nuevoProducto(new BigDecimal("1000.00"), 3, 0);

        cartService.addItem(comprador, item(producto, 2));

        // 2 que ya tenia + 2 nuevas = 4, y solo hay 3.
        Assertions.assertThrows(InsufficientStockException.class,
                () -> cartService.addItem(comprador, item(producto, 2)));
    }

    @Test
    void modificarLaCantidadRecalculaElTotal() throws Exception {
        Long comprador = nuevoComprador();
        Long producto = nuevoProducto(new BigDecimal("1000.00"), 50, 0);

        CartResponse carrito = cartService.addItem(comprador, item(producto, 2));
        Long linea = carrito.getItems().get(0).getId();

        CartItemUpdateRequest request = new CartItemUpdateRequest();
        request.setQuantity(5);
        CartResponse actualizado = cartService.updateItem(comprador, linea, request);

        assertThat(actualizado.getItems().get(0).getQuantity()).isEqualTo(5);
        assertThat(actualizado.getTotal()).isEqualByComparingTo(new BigDecimal("5000.00"));
    }

    @Test
    void eliminarUnaLineaBajaElTotal() throws Exception {
        Long comprador = nuevoComprador();
        Long a = nuevoProducto(new BigDecimal("1000.00"), 50, 0);
        Long b = nuevoProducto(new BigDecimal("2000.00"), 50, 0);

        cartService.addItem(comprador, item(a, 1));
        CartResponse carrito = cartService.addItem(comprador, item(b, 1));
        Long lineaB = carrito.getItems().stream()
                .filter(i -> i.getProductId().equals(b)).findFirst().orElseThrow().getId();

        CartResponse despues = cartService.removeItem(comprador, lineaB);

        assertThat(despues.getItems()).hasSize(1);
        assertThat(despues.getTotal()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void vaciarElCarritoLoDejaEnCero() throws Exception {
        Long comprador = nuevoComprador();
        Long producto = nuevoProducto(new BigDecimal("1000.00"), 50, 0);

        cartService.addItem(comprador, item(producto, 3));
        CartResponse vacio = cartService.clearCart(comprador);

        assertThat(vacio.getItems()).isEmpty();
        assertThat(vacio.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void noSePuedeTocarUnaLineaQueNoExiste() throws Exception {
        Long comprador = nuevoComprador();

        CartItemUpdateRequest request = new CartItemUpdateRequest();
        request.setQuantity(1);

        Assertions.assertThrows(CartItemNotFoundException.class,
                () -> cartService.updateItem(comprador, 999999L, request));
        Assertions.assertThrows(CartItemNotFoundException.class,
                () -> cartService.removeItem(comprador, 999999L));
    }

    // =========================================================== CHECKOUT

    @Test
    void elCheckoutCalculaElTotalConLosDescuentosVigentes() throws Exception {
        Long comprador = nuevoComprador();
        Long a = nuevoProducto(new BigDecimal("10000.00"), 50, 20);  // 2 x 8000 = 16000
        Long b = nuevoProducto(new BigDecimal("5000.00"), 50, 0);    // 1 x 5000 =  5000

        cartService.addItem(comprador, item(a, 2));
        cartService.addItem(comprador, item(b, 1));

        OrderResponse orden = orderService.checkout(comprador);

        assertThat(orden.getId()).isNotNull();
        assertThat(orden.getItems()).hasSize(2);
        assertThat(orden.getTotal()).isEqualByComparingTo(new BigDecimal("21000.00"));
    }

    @Test
    void elCheckoutDescuentaElStockYVaciaElCarrito() throws Exception {
        Long comprador = nuevoComprador();
        Long producto = nuevoProducto(new BigDecimal("1000.00"), 10, 0);

        cartService.addItem(comprador, item(producto, 4));
        orderService.checkout(comprador);

        assertThat(stockDe(producto)).isEqualTo(6);
        assertThat(cartService.getCart(comprador).getItems()).isEmpty();
    }

    @Test
    void noSePuedeHacerCheckoutConElCarritoVacio() throws Exception {
        Long comprador = nuevoComprador();

        EmptyCartException error = Assertions.assertThrows(EmptyCartException.class,
                () -> orderService.checkout(comprador));

        assertThat(error.getMessage()).contains("vacío");
    }

    /**
     * El test mas importante de esta parte: ATOMICIDAD.
     *
     * Se arma un carrito con dos productos y, antes del checkout, se deja al
     * segundo sin stock suficiente (como si otra persona lo hubiera comprado
     * en el medio). El checkout tiene que fallar entero: el primer producto NO
     * puede quedar con el stock descontado.
     *
     * Esto es lo que garantiza el @Transactional del checkout.
     */
    @Test
    void siFallaUnaLineaNoSeDescuentaElStockDeNinguna() throws Exception {
        Long comprador = nuevoComprador();
        Long primero = nuevoProducto(new BigDecimal("1000.00"), 10, 0);
        Long segundo = nuevoProducto(new BigDecimal("1000.00"), 10, 0);

        cartService.addItem(comprador, item(primero, 2));
        cartService.addItem(comprador, item(segundo, 5));

        // Alguien mas se lleva casi todo el stock del segundo producto.
        Product p = productRepository.findById(segundo).orElseThrow();
        p.setStock(1);
        productRepository.save(p);

        Assertions.assertThrows(InsufficientStockException.class,
                () -> orderService.checkout(comprador));

        // Si la transaccion hizo rollback, el primero sigue intacto en 10.
        assertThat(stockDe(primero)).isEqualTo(10);
        // Y el carrito no se vacio.
        assertThat(cartService.getCart(comprador).getItems()).hasSize(2);
    }

    // =========================================================== ORDENES

    @Test
    void laOrdenGuardaElPrecioDelMomentoDeLaCompra() throws Exception {
        Long comprador = nuevoComprador();
        Long producto = nuevoProducto(new BigDecimal("1000.00"), 10, 0);

        cartService.addItem(comprador, item(producto, 2));
        OrderResponse orden = orderService.checkout(comprador);

        // Despues de la compra la tienda cambia el precio.
        Product p = productRepository.findById(producto).orElseThrow();
        p.setPrice(new BigDecimal("9999.00"));
        productRepository.save(p);

        OrderResponse guardada = orderService.getOrderById(orden.getId(), comprador);

        // La orden no cambia: guarda el precio con el que se compro.
        assertThat(guardada.getItems().get(0).getUnitPrice()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(guardada.getTotal()).isEqualByComparingTo(new BigDecimal("2000.00"));
    }

    @Test
    void elHistorialSoloTraeLasOrdenesDelUsuario() throws Exception {
        Long unComprador = nuevoComprador();
        Long otroComprador = nuevoComprador();
        Long producto = nuevoProducto(new BigDecimal("1000.00"), 50, 0);

        cartService.addItem(unComprador, item(producto, 1));
        orderService.checkout(unComprador);

        assertThat(orderService.getOrdersByUser(unComprador, PageRequest.of(0, 10)).getTotalElements())
                .isEqualTo(1);
        assertThat(orderService.getOrdersByUser(otroComprador, PageRequest.of(0, 10)).getTotalElements())
                .isZero();
    }

    @Test
    void unCompradorNoPuedeVerLaOrdenDeOtro() throws Exception {
        Long duenio = nuevoComprador();
        Long ajeno = nuevoComprador();
        Long producto = nuevoProducto(new BigDecimal("1000.00"), 50, 0);

        cartService.addItem(duenio, item(producto, 1));
        OrderResponse orden = orderService.checkout(duenio);

        // Devuelve 404 y no 403: no le confirma a nadie que esa orden existe.
        Assertions.assertThrows(OrderNotFoundException.class,
                () -> orderService.getOrderById(orden.getId(), ajeno));
    }
}
