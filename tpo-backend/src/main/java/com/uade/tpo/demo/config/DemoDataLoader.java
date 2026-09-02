package com.uade.tpo.demo.config;

import java.math.BigDecimal;
import java.util.List;

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
 * Carga un catalogo de ejemplo para desarrollo y para los tests automaticos.
 *
 * VIENE APAGADO (app.seed-demo-data=false). Los datos de prueba pueden
 * crearse desde Insomnia, comenzando con la base vacia y agregando
 * categorias, productos y compradores.
 *
 * Se enciende con app.seed-demo-data=true cuando conviene tener datos a mano.
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
public class DemoDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);
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
        if (productRepository.count() > 0) {
            log.info("Ya hay productos cargados, no se generan los de ejemplo.");
            return;
        }

        createBuyer("sofia", "sofia@mail.com", "Sofia", "Gomez");
        createBuyer("martin", "martin@mail.com", "Martin", "Alvarez");

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

        log.info("Catalogo de ejemplo cargado (app.seed-demo-data=true).");
    }

    private void createBuyer(String nickname, String email, String name, String surname) {
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
