package com.uade.tpo.demo.config;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Cart;
import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.ProductImage;
import com.uade.tpo.demo.entity.Role;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.repository.CartRepository;
import com.uade.tpo.demo.repository.CategoryRepository;
import com.uade.tpo.demo.repository.ProductRepository;
import com.uade.tpo.demo.repository.UserRepository;

/**
 * Carga el catalogo inicial de Mesa Llena la primera vez que arranca la
 * aplicacion contra una base vacia.
 *
 * Por que existe: los productos viven en la base de datos, no en el repositorio.
 * Si cada integrante del grupo clona el proyecto y levanta su propio MySQL, se
 * encuentra con un catalogo vacio y tiene que cargar todo a mano antes de poder
 * ver nada. Con esto, clonar y levantar alcanza para tener la tienda andando.
 *
 * Solo corre si la tabla de productos esta vacia: nunca pisa datos existentes.
 * Se puede apagar poniendo app.seed-demo-data=false en application.properties.
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true", matchIfMissing = true)
public class DemoDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    /** Carpeta dentro de resources donde estan las tapas de los juegos. */
    private static final String CARPETA_IMAGENES = "/demo-images/";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Si ya hay productos, no tocamos nada. Esto hace que arrancar la
        // aplicacion dos veces no duplique el catalogo, y que los productos que
        // se carguen despues desde el panel no corran riesgo.
        if (productRepository.count() > 0) {
            log.info("La base ya tiene productos: no se carga el catalogo inicial.");
            return;
        }

        log.info("Base vacia: cargando el catalogo inicial de Mesa Llena...");

        // Dos compradores de ejemplo, para poder probar el carrito sin tener
        // que registrarse primero. La contrasenia de los dos es 123456.
        crearComprador("sofia", "sofia@mail.com", "Sofia", "Gomez");
        crearComprador("martin", "martin@mail.com", "Martin", "Alvarez");

        Category estrategia = crearCategoria("Estrategia");
        Category familiar = crearCategoria("Familiar");
        Category cartas = crearCategoria("Cartas");
        Category party = crearCategoria("Party games");

        crearProducto("Catan",
                "Juego de estrategia y negociacion para 3 a 4 jugadores. Edicion en espaniol. "
                        + "Partidas de 60 a 90 minutos.",
                new BigDecimal("54999.00"), 12, 15, estrategia, "catan.png");

        crearProducto("Carcassonne",
                "Juego de colocacion de losetas para 2 a 5 jugadores. Partidas de 35 minutos. "
                        + "Incluye la expansion Rio.",
                new BigDecimal("42500.00"), 7, 10, estrategia, "carcassonne.png");

        crearProducto("Ticket to Ride",
                "Carrera de trenes por Estados Unidos. De 2 a 5 jugadores, 45 minutos. "
                        + "Facil de aprender, dificil de soltar.",
                new BigDecimal("48900.00"), 5, 0, estrategia, "ticket.png");

        // Este queda sin stock a proposito: sirve para mostrar que el catalogo
        // lo marca como agotado y que no se puede agregar al carrito.
        crearProducto("Azul",
                "Juego abstracto de colocacion de azulejos. De 2 a 4 jugadores, 35 minutos.",
                new BigDecimal("39500.00"), 0, 0, estrategia, "azul.png");

        crearProducto("Dixit",
                "Juego de cartas ilustradas y asociaciones libres. De 3 a 6 jugadores, 30 minutos.",
                new BigDecimal("44900.00"), 9, 20, cartas, "dixit.png");

        crearProducto("Uno",
                "El clasico juego de cartas. De 2 a 10 jugadores, partidas rapidas de 15 minutos. "
                        + "Mazo de 108 cartas.",
                new BigDecimal("9900.00"), 40, 0, cartas, "uno.png");

        crearProducto("Codenames",
                "Juego de palabras por equipos. De 4 a 8 jugadores, 15 minutos por partida.",
                new BigDecimal("29900.00"), 14, 0, party, "codenames.png");

        crearProducto("Jenga",
                "La torre de maderitas que todos conocen. Desde 1 jugador, para cualquier edad.",
                new BigDecimal("18500.00"), 22, 5, familiar, "jenga.png");

        log.info("Catalogo inicial cargado: {} productos en {} categorias.",
                productRepository.count(), categoryRepository.count());
    }

    private void crearComprador(String nickname, String email, String name, String surname) {
        if (userRepository.existsByEmail(email))
            return;

        User user = new User();
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setName(name);
        user.setSurname(surname);
        user.setRole(Role.USER);

        cartRepository.save(new Cart(userRepository.save(user)));
    }

    private Category crearCategoria(String description) {
        return categoryRepository.findByDescription(description).stream()
                .findFirst()
                .orElseGet(() -> categoryRepository.save(new Category(description)));
    }

    private void crearProducto(String name, String description, BigDecimal price, int stock,
            int discount, Category category, String archivoImagen) {

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setDiscount(discount);
        product.setCategory(category);
        product.setActive(true);

        product.getImages().add(new ProductImage(leerImagen(archivoImagen), product));

        productRepository.save(product);
    }

    /**
     * Lee una tapa de src/main/resources/demo-images y la convierte en el Blob
     * que guarda la base.
     *
     * Se lee como recurso del classpath y no como archivo del disco: asi sigue
     * funcionando cuando la aplicacion se empaqueta en un .jar, donde las
     * imagenes quedan adentro del jar y ya no existen como archivos sueltos.
     */
    private Blob leerImagen(String archivo) {
        String ruta = CARPETA_IMAGENES + archivo;

        try (InputStream entrada = getClass().getResourceAsStream(ruta)) {
            if (entrada == null)
                throw new IllegalStateException("No se encontro la imagen " + ruta);

            return new SerialBlob(entrada.readAllBytes());

        } catch (IOException | SQLException e) {
            throw new IllegalStateException("No se pudo leer la imagen " + ruta, e);
        }
    }
}
