package com.uade.tpo.demo.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.ProductImage;
import com.uade.tpo.demo.entity.Role;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.repository.CategoryRepository;
import com.uade.tpo.demo.repository.ProductRepository;
import com.uade.tpo.demo.repository.RoleRepository;
import com.uade.tpo.demo.repository.UserRepository;

/**
 * Carga datos de prueba la primera vez que se levanta la aplicacion, para poder
 * demostrar el CRUD completo desde Insomnia sin tener que cargar todo a mano.
 *
 * Las contrasenias van en texto plano a proposito: el register con hasheo lo
 * entrega la catedra en la clase de seguridad. Cuando eso se integre, este
 * componente se elimina o se apaga con app.seed-demo-data=false.
 */
@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    /** Placeholder que si carga en el navegador, para poder ver la imagen en la demo. */
    private static final String IMAGE_URL = "https://placehold.co/600x600/1f2937/ffffff?text=";

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Ya hay datos cargados, no se vuelven a generar los datos de prueba.");
            return;
        }

        Role user = roleRepository.findByDescription("USER").orElseGet(() -> roleRepository.save(new Role("USER")));
        Role admin = roleRepository.findByDescription("ADMIN").orElseGet(() -> roleRepository.save(new Role("ADMIN")));

        // La tienda tiene un unico vendedor: la administracion del sitio.
        User tienda = createUser("admin", "admin@juegosdemesa.com", "Tomas", "Szkarlatiuk", List.of(user, admin));
        User sofia = createUser("sofia", "sofia@mail.com", "Sofia", "Gomez", List.of(user));
        User martin = createUser("martin", "martin@mail.com", "Martin", "Alvarez", List.of(user));

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
        log.info("Usuarios -> admin (la tienda) id={} | sofia id={} | martin id={}",
                tienda.getId(), sofia.getId(), martin.getId());
        log.info("Nota: 'Uno' se carga con stock 0 a proposito, para probar la validacion del carrito.");
    }

    private User createUser(String username, String email, String name, String surname, List<Role> roles) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("123456");
        u.setName(name);
        u.setSurname(surname);
        u.setRoles(new ArrayList<>(roles));
        return userRepository.save(u);
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
