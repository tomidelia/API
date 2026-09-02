package com.uade.tpo.demo.config;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
 * Carga datos de prueba la primera vez que se levanta la aplicacion, para poder
 * demostrar todo desde Insomnia sin cargar nada a mano.
 *
 * La cuenta de la tienda (ADMIN) se crea aca a proposito: el registro publico
 * siempre da rol USER, asi que un comprador no puede auto-asignarse ADMIN.
 * Las contrasenias se guardan hasheadas con BCrypt, igual que en el registro.
 *
 * Se apaga con app.seed-demo-data=false.
 */
@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    /** Placeholder que si carga en el navegador, para poder ver la imagen. */
    private static final String IMAGE_URL = "https://placehold.co/600x600/1f2937/ffffff?text=";

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
        if (userRepository.count() > 0) {
            log.info("Ya hay datos cargados, no se vuelven a generar los datos de prueba.");
            return;
        }

        createUser("admin", "admin@juegosdemesa.com", "Tienda", "Juegos de Mesa", Role.ADMIN);
        createUser("sofia", "sofia@mail.com", "Sofia", "Gomez", Role.USER);
        createUser("martin", "martin@mail.com", "Martin", "Alvarez", Role.USER);

        Category estrategia = createCategory("Estrategia");
        Category familiar = createCategory("Familiar");
        Category cartas = createCategory("Cartas");
        createCategory("Party games");

        createProduct("Catan", "Juego de estrategia y negociacion para 3 a 4 jugadores. Edicion en espaniol.",
                new BigDecimal("54999.00"), 12, 0, estrategia, List.of("Catan", "Catan+caja"));

        createProduct("Carcassonne", "Juego de colocacion de losetas. Incluye la expansion Rio.",
                new BigDecimal("42500.00"), 7, 15, estrategia, List.of("Carcassonne"));

        createProduct("Dixit", "Juego de cartas ilustradas y asociacion de ideas para toda la familia.",
                new BigDecimal("61000.00"), 4, 0, familiar, List.of("Dixit"));

        createProduct("Uno", "El clasico juego de cartas. Mazo de 108 cartas.",
                new BigDecimal("8900.00"), 0, 0, cartas, List.of("Uno"));

        createProduct("Virus!", "Juego de cartas rapido para 2 a 6 jugadores.",
                new BigDecimal("15400.00"), 25, 20, cartas, List.of("Virus"));

        log.info("Datos de prueba cargados.");
        log.info("Cuentas: admin@juegosdemesa.com (ADMIN) | sofia@mail.com (USER) | martin@mail.com (USER)");
        log.info("Contrasenia de todas: 123456");
        log.info("Nota: 'Uno' se carga con stock 0 a proposito, para probar la validacion del carrito.");
    }

    private void createUser(String nickname, String email, String name, String surname, Role role) {
        User user = new User();
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setName(name);
        user.setSurname(surname);
        user.setRole(role);

        User saved = userRepository.save(user);

        // Igual que en el registro: el carrito se crea junto con el usuario.
        if (role == Role.USER)
            cartRepository.save(new Cart(saved));
    }

    private Category createCategory(String description) {
        return categoryRepository.findByDescription(description).stream()
                .findFirst()
                .orElseGet(() -> categoryRepository.save(new Category(description)));
    }

    private void createProduct(String name, String description, BigDecimal price, int stock, int discount,
            Category category, List<String> imageLabels) {

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setDiscount(discount);
        product.setCategory(category);
        product.setActive(true);

        for (String label : imageLabels)
            product.getImages().add(new ProductImage(IMAGE_URL + label, product));

        productRepository.save(product);
    }
}
